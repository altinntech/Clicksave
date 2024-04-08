package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.dto.*;
import com.altinntech.clicksave.core.utils.ClicksaveSequence;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.core.utils.tb.TableAdditionsResolver;
import com.altinntech.clicksave.core.utils.tb.TableBuilder;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.EntityInitializationException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import org.reflections.Reflections;

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
 * <p>This class is annotated with {@code @Component} for Spring dependency injection.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
//@Component
public class CSBootstrap {

    private Set<Class<?>> entityClasses;
    private final Map<Class<?>, ClassDataCache> classDataCacheMap = new HashMap<>();
    private final Map<Class<?>, EmbeddableClassData> embeddableClassDataCacheMap = new HashMap<>();

    private final ConnectionManager connectionManager;
    private final BatchCollector batchCollector;
    private static CSBootstrap instance;

    private DefaultProperties defaultProperties;

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
        this.defaultProperties = defaultProperties;
        instance = this;
        this.connectionManager = new ConnectionManager(defaultProperties);
        this.batchCollector = BatchCollector.getInstance();
        if (defaultProperties.validate()) {
            initialize();
            info("Initializing completed");
        } else {
            warn("Initialization skipped due to unrecognized properties. Check the configuration to ensure that all properties are correctly spelled and recognized");
        }
    }

    public static CSBootstrap getInstance() {
        return instance;
    }

    private void initialize() throws FieldInitializationException, ClassCacheNotFoundException {
        Reflections reflections = new Reflections(defaultProperties.getRootPackageToScan());
        entityClasses = reflections.getTypesAnnotatedWith(ClickHouseEntity.class);
        entityClasses.add(ClicksaveSequence.class);
        Set<Class<?>> embeddableClasses = reflections.getTypesAnnotatedWith(Embeddable.class);

        for (Class<?> clazz : entityClasses) {
            ClassDataCache classDataCache = new ClassDataCache();
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

            classDataCacheMap.put(clazz, classDataCache);
            debug("Find entity class: " + clazz);
        }

        for (Class<?> clazz : embeddableClasses) {
            EmbeddableClassData embeddableClassData = new EmbeddableClassData();
            PreparedFieldsData preparedFieldsData = getFieldsData(clazz);
            embeddableClassData.setFields(preparedFieldsData.getFields());

            embeddableClassDataCacheMap.put(clazz, embeddableClassData);
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


    private void shutdownProcess() {
        info("Shutdown initiated! Saving batches...");
        try {
            batchCollector.saveAndFlushAll();
            connectionManager.closeAllConnections();
            info("Shutdown completed");
        } catch (SQLException | ClassCacheNotFoundException | IllegalAccessException e) {
            error("Error while saving batches: " + e.getMessage());
        }
    }

    /**
     * Retrieves the class data cache for the specified class.
     *
     * @param clazz the class
     * @return the class data cache
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    public ClassDataCache getClassDataCache(Class<?> clazz) throws ClassCacheNotFoundException {
        Optional<ClassDataCache> classDataCacheOptional = Optional.ofNullable(classDataCacheMap.get(clazz));
        if (classDataCacheOptional.isPresent()) {
            return classDataCacheOptional.get();
        }
        throw new ClassCacheNotFoundException();
    }

    /**
     * Retrieves the embeddable class data cache for the specified class.
     *
     * @param clazz the class
     * @return the embeddable class data cache
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    public EmbeddableClassData getEmbeddableClassDataCache(Class<?> clazz) throws ClassCacheNotFoundException {
        Optional<EmbeddableClassData> classDataCacheOptional = Optional.ofNullable(embeddableClassDataCacheMap.get(clazz));
        if (classDataCacheOptional.isPresent()) {
            return classDataCacheOptional.get();
        }
        throw new ClassCacheNotFoundException();
    }

    /**
     * Retrieves the batch collector.
     *
     * @return the batch collector
     */
    public BatchCollector getBatchCollector() {
        return batchCollector;
    }

    /**
     * Retrieves a database connection.
     *
     * @return the database connection
     * @throws SQLException if a SQL exception occurs
     */
    public Connection getConnection() throws SQLException {
        return connectionManager.getConnection();
    }

    /**
     * Releases a database connection.
     *
     * @param connection the database connection to release
     * @throws SQLException if a SQL exception occurs
     */
    public void releaseConnection(Connection connection) throws SQLException {
        connectionManager.releaseConnection(connection);
    }

    private void createTablesFromAnnotatedClasses() throws FieldInitializationException, ClassCacheNotFoundException {
        for (Class<?> clazz : entityClasses) {
            if (clazz.getAnnotation(ClickHouseEntity.class) != null && clazz.getAnnotation(ClickHouseEntity.class).forTest() && !Boolean.parseBoolean(defaultProperties.getTestEnv()))
                continue;
            ClassDataCache classDataCache = getClassDataCache(clazz);
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
