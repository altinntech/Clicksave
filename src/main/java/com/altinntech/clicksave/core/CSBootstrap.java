package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.dto.*;
import com.altinntech.clicksave.core.query.executor.QueryExecutor;
import com.altinntech.clicksave.core.utils.ClicksaveSequence;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.core.utils.tb.TableBuilder;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.EntityInitializationException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import com.altinntech.clicksave.metrics.MonitoringService;
import lombok.Getter;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.altinntech.clicksave.core.CSUtils.*;
import static com.altinntech.clicksave.log.CSLogger.*;

/**
 * The {@code CSBootstrap} class initializes the application and manages the configuration setup.
 * It sets up connections, initializes entities, and handles shutdown procedures.
 *
 *
 * @author Fyodor Plotnikov
 */
@Configuration
public class CSBootstrap {

    private Set<Class<?>> entityClasses;
    private final ConnectionManager connectionManager;
    @Getter
    private final CHRepository repository;
    private final IdsManager idsManager;
    private final BatchCollector batchCollector;
    private final ThreadPoolManager threadPoolManager;
    private final QueryExecutor queryExecutor;
    @Getter
    private final ClassDataCacheService classDataCacheService;
    private final MonitoringService monitoringService;
    private final SyncManager syncManager;

    private final DefaultProperties defaultProperties;

    private boolean isDisposed = false;

    /**
     * Constructs a new CSBootstrap instance.
     *
     * @throws FieldInitializationException if field initialization fails
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    public CSBootstrap() throws FieldInitializationException, ClassCacheNotFoundException, SQLException {
        this(DefaultProperties.fromPropertyFile());
    }

    public CSBootstrap(DefaultProperties defaultProperties) throws FieldInitializationException, ClassCacheNotFoundException, SQLException {
        info("Start initialization...");

        this.classDataCacheService = new ClassDataCacheService();
        this.defaultProperties = defaultProperties;
        this.connectionManager = new ConnectionManager(defaultProperties);
        this.idsManager = new IdsManager(connectionManager);
        this.batchCollector = BatchCollector.create(idsManager, connectionManager, defaultProperties);
        this.threadPoolManager = new ThreadPoolManager(defaultProperties);
        this.syncManager = SyncManager.create(defaultProperties, batchCollector);
        this.repository = new CHRepository(connectionManager, classDataCacheService, batchCollector, idsManager, threadPoolManager, syncManager);
        this.queryExecutor = new QueryExecutor(connectionManager, classDataCacheService, batchCollector, syncManager, threadPoolManager);
        this.monitoringService = new MonitoringService(connectionManager, threadPoolManager, syncManager, defaultProperties);
        idsManager.setRepository(repository);

        if (defaultProperties.validate()) {
            initialize();
            info("Initializing completed");
        } else {
            warn("Initialization skipped due to unrecognized properties. Check the configuration to ensure that all properties are correctly spelled and recognized");
        }
    }

    @Bean
    public MonitoringService monitoringService() {
        return monitoringService;
    }

    private synchronized void dispose() {
        isDisposed = true;
        this.batchCollector.dispose();
        this.classDataCacheService.dispose();
        this.syncManager.dispose();
        this.entityClasses.clear();
        info("CSBootstrap", "Used resources disposed");
    }

    public ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public QueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    private void initialize() throws FieldInitializationException, ClassCacheNotFoundException {
        Reflections reflections = new Reflections(defaultProperties.getRootPackageToScan());
        entityClasses = reflections.getTypesAnnotatedWith(ClickHouseEntity.class);
        entityClasses.add(ClicksaveSequence.class);
        Set<Class<?>> embeddableClasses = reflections.getTypesAnnotatedWith(Embeddable.class);

        for (Class<?> clazz : entityClasses) {
            ClassDataCache classDataCache = new ClassDataCache();
            classDataCache.setEntityClass(clazz);
            classDataCache.setTableName(buildTableName(clazz));
            classDataCache.setBatchingAnnotation(clazz.getAnnotation(Batching.class));
            classDataCache.setPartitionByAnnotation(clazz.getAnnotation(PartitionBy.class));
            classDataCache.setOrderByAnnotation(clazz.getAnnotation(OrderBy.class));
            classDataCache.setSystemTableAnnotation(clazz.getAnnotation(SystemTable.class));
            classDataCache.setCHEAnnotation(clazz.getAnnotation(ClickHouseEntity.class));

            PreparedFieldsData preparedFieldsData = getFieldsData(clazz);
            if (preparedFieldsData.getIdFieldsCount() != 1) {
                throw new EntityInitializationException("Entity must have at least one id field: " + classDataCache.getTableName());
            }
            classDataCache.setFields(preparedFieldsData.getFields());
            classDataCache.setIdField(preparedFieldsData.getIdField());
            classDataCache.setMethodData(getMethodData(clazz));

            classDataCacheService.putClassDataCache(clazz, classDataCache);
            debug("Find entity class: " + clazz);
        }

        for (Class<?> clazz : embeddableClasses) {
            EmbeddableClassData embeddableClassData = new EmbeddableClassData();
            PreparedFieldsData preparedFieldsData = getFieldsData(clazz);
            embeddableClassData.setFields(preparedFieldsData.getFields());

            classDataCacheService.putEmbeddableClassDataCache(clazz, embeddableClassData);
            debug("Find embeddable class: " + clazz);
        }

        createTablesFromAnnotatedClasses();
        shutdownThread.setName("CS_shutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * The shutdown thread.
     */
    Thread shutdownThread = new Thread(this::shutdownProcess);


    private synchronized void shutdownProcess() {
        if (isDisposed) {
            return;
        }
        info("Shutdown initiated! Saving batches...");
        try {
            threadPoolManager.shutdown();
            batchCollector.saveAndFlushAll();
            connectionManager.closeAllConnections();
            syncManager.shutdown();
            dispose();
            info("Shutdown completed");
        } catch (SQLException | ClassCacheNotFoundException | IllegalAccessException | InvocationTargetException e) {
            error("Error while stopping: " + e.getMessage());
        }
    }

    private void createTablesFromAnnotatedClasses() throws FieldInitializationException, ClassCacheNotFoundException {
        for (Class<?> clazz : entityClasses) {
            if (clazz.getAnnotation(ClickHouseEntity.class) != null && clazz.getAnnotation(ClickHouseEntity.class).forTest() && !Boolean.parseBoolean(defaultProperties.getTestEnv()))
                continue;
            ClassDataCache classDataCache = classDataCacheService.getClassDataCache(clazz);
            String tableName = classDataCache.getTableName();
            TableBuilder tb = new TableBuilder(this);
            if (!isTableExists(tableName)) {
                tb.generateTable(clazz);
                info("Table created: " + tableName);
            } else {
                tb.updateTable(clazz);
            }
        }
    }

    private boolean isTableExists(String tableName) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLES LIKE ?")) {

            preparedStatement.setString(1, tableName);
            boolean isTableExists = false;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    isTableExists = true;
                }
                connectionManager.releaseConnection(connection);
                return isTableExists;
            }
        } catch (SQLException e) {
            error(e.getMessage(), this.getClass());
        }
        return false;
    }

    /**
     * Fetches the list of columns for a table.
     *
     * @param tableName the table name
     * @return the list of columns
     */
    public List<ColumnData> fetchTableColumns(String tableName) {
        List<ColumnData> columns = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT name, type FROM system.columns WHERE table = ?")) {

            preparedStatement.setString(1, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String type = resultSet.getString("type");
                    columns.add(new ColumnData(name, type));
                }
                connectionManager.releaseConnection(connection);
            }

        } catch (SQLException e) {
            error(e.getMessage(), this.getClass());
        }

        return columns;
    }

    public DefaultProperties getDefaultProperties() {
        return defaultProperties;
    }

    public void executeQuery(String query) {
        try (Connection connection = connectionManager.getConnection()) {
            connection.createStatement().execute(query);
            connectionManager.releaseConnection(connection);
        } catch (SQLException e) {
            error("Query execution error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
