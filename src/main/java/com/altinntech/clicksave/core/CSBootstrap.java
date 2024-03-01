package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.EmbeddableClassData;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
@Component
public class CSBootstrap {

    private Set<Class<?>> entityClasses;
    private final Map<Class<?>, ClassDataCache> classDataCacheMap = new HashMap<>();
    private final Map<Class<?>, EmbeddableClassData> embeddableClassDataCacheMap = new HashMap<>();

    private final ConnectionManager connectionManager;
    private final Environment environment;
    private final BatchCollector batchCollector;
    private static CSBootstrap instance;

    /**
     * Constructs a new CSBootstrap instance.
     *
     * @param connectionManager the connection manager
     * @param environment       the environment
     * @param batchCollector    the batch collector
     * @throws FieldInitializationException if field initialization fails
     * @throws ClassCacheNotFoundException if class cache is not found
     */
    @Autowired
    public CSBootstrap(ConnectionManager connectionManager, Environment environment, @Lazy BatchCollector batchCollector) throws FieldInitializationException, ClassCacheNotFoundException {
        this.connectionManager = connectionManager;
        this.environment = environment;
        this.batchCollector = batchCollector;
        instance = this;
        initialize();
    }

    public static CSBootstrap getInstance() {
        return instance;
    }

    private void initialize() throws FieldInitializationException, ClassCacheNotFoundException {
        info("Start initialization...");
        DefaultProperties defaultProperties = new DefaultProperties(environment);
        Reflections reflections = new Reflections(defaultProperties.getRootPackageToScan());
        entityClasses = reflections.getTypesAnnotatedWith(ClickHouseEntity.class);
        Set<Class<?>> embeddableClasses = reflections.getTypesAnnotatedWith(Embeddable.class);

        for (Class<?> clazz : entityClasses) {
            ClassDataCache classDataCache = new ClassDataCache();

            classDataCache.setTableName(buildTableName(clazz));
            classDataCache.setBatchingAnnotation(clazz.getAnnotation(Batching.class));
            getFieldsData(clazz, classDataCache);

            classDataCacheMap.put(clazz, classDataCache);
            debug("Find class: " + clazz);
        }

        for (Class<?> clazz : embeddableClasses) {
            EmbeddableClassData embeddableClassData = new EmbeddableClassData();
            getFieldsData(clazz, embeddableClassData);

            embeddableClassDataCacheMap.put(clazz, embeddableClassData);
            debug("Find embeddable class: " + clazz);
        }

        createTablesFromAnnotatedClasses();
        shutdownThread.setName("CS_shutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        info("Initializing completed");
    }

    /**
     * The shutdown thread.
     */
    Thread shutdownThread = new Thread(this::shutdownProcess);


    private void shutdownProcess() {
        info("Shutdown initiated! Saving batches...");
        try {
            batchCollector.saveAndFlushAll();
        } catch (SQLException | ClassCacheNotFoundException | IllegalAccessException e) {
            error(e.getMessage());
        }
        connectionManager.closeAllConnections();
        info("Shutdown completed");
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
            ClassDataCache classDataCache = getClassDataCache(clazz);
            String tableName = classDataCache.getTableName();
            if (!isTableExists(tableName)) {
                String createTableQuery = generateCreateTableQuery(clazz);
                executeQuery(createTableQuery);
            } else {
                updateTable(clazz);
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
            error(e.getMessage());
        }
        return false;
    }

    /**
     * Fetches the list of columns for a table.
     *
     * @param tableName the table name
     * @return the list of columns
     */
    public List<String> fetchTableColumns(String tableName) {
        List<String> columns = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM system.columns WHERE table = ?")) {

            preparedStatement.setString(1, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next()) {
                    columns.add(resultSet.getString("name"));
                }
                connectionManager.releaseConnection(connection);
            }

        } catch (SQLException e) {
            error(e.getMessage());
        }

        return columns;
    }

    private String generateCreateTableQuery(Class<?> clazz) throws FieldInitializationException, ClassCacheNotFoundException {
        StringBuilder primaryKey = new StringBuilder();

        ClassDataCache classDataCache = getClassDataCache(clazz);

        String tableName = classDataCache.getTableName();
        List<FieldDataCache> fields = classDataCache.getFields();
        info("Create table " + tableName);

        StringBuilder query = new StringBuilder("CREATE TABLE ");
        query.append(tableName).append(" (");

        parseQueryForCreate(primaryKey, fields, query);

        query.delete(query.length() - 2, query.length()).append(") ");
        query.append("ENGINE = MergeTree ").append("PRIMARY KEY (").append(primaryKey).append(")");
        return query.toString();
    }

    private void parseQueryForCreate(StringBuilder primaryKey, List<FieldDataCache> fields, StringBuilder query) {
        for (FieldDataCache fieldData : fields) {
            String fieldName = fieldData.getFieldInTableName();
            Optional<Column> columnOptional = fieldData.getColumnAnnotation();
            Optional<EnumColumn> enumeratedOptional = fieldData.getEnumColumnAnnotation();
            Optional<Embedded> embeddedOptional = fieldData.getEmbeddedAnnotation();
            Optional<Lob> lobOptional = fieldData.getLobAnnotation();

            if (enumeratedOptional.isPresent()) {
                EnumColumn enumeratedAnnotation = enumeratedOptional.get();
                if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                    query.append(fieldName).append(" ");
                    query.append(FieldType.UINT16.getType());
                } else if (enumeratedAnnotation.value() == EnumType.STRING) {
                    query.append(fieldName).append(" ");
                    query.append(FieldType.STRING.getType());
                } else {
                    query.append(fieldName).append(" ");
                    query.append(FieldType.LONG.getType());
                }
            } else if (columnOptional.isPresent()) {
                Column columnAnnotation = columnOptional.get();
                String dataType = columnAnnotation.value().getType();
                query.append(fieldName).append(" ");
                query.append(dataType);

                if (columnAnnotation.primaryKey() || columnAnnotation.id()) {
                    if (primaryKey.length() == 0) {
                        primaryKey.append(fieldName);
                    } else {
                        primaryKey.append(", ").append(fieldName);
                    }
                }
            } else if (embeddedOptional.isPresent()) {
                EmbeddableClassData embeddableClassData = embeddableClassDataCacheMap.get(fieldData.getType());
                if (embeddableClassData != null) {
                    parseQueryForCreate(primaryKey, embeddableClassData.getFields(), query);
                    continue;
                } else {
                    throw new FieldInitializationException("Embeddable class of field '" + fieldData.getFieldName() + "' not found");
                }
            } else if (lobOptional.isPresent()) {
                query.append(fieldName).append(" ");
                query.append(FieldType.STRING.getType());
            }
            else {
                throw new FieldInitializationException("Not valid field: " + fieldData);
            }

            query.append(", ");
        }
    }

    private void updateTable(Class<?> clazz) throws FieldInitializationException, ClassCacheNotFoundException {
        ClassDataCache classDataCache = getClassDataCache(clazz);

        String tableName = classDataCache.getTableName();
        List<String> tableFieldsFromDB = fetchTableColumns(tableName);
        info("Check for updates table " + tableName);

        List<FieldDataCache> fields = classDataCache.getFields();
        for (FieldDataCache fieldData : fields) {
            String fieldName = fieldData.getFieldInTableName();
            if (!tableFieldsFromDB.contains(fieldName) && fieldData.getEmbeddedAnnotation().isEmpty()) {
                info("Update field " + fieldName + " in table " + tableName);
                Optional<Column> columnOptional = fieldData.getColumnAnnotation();
                Optional<EnumColumn> enumeratedOptional = fieldData.getEnumColumnAnnotation();
                Optional<Lob> lobOptional = fieldData.getLobAnnotation();

                if (columnOptional.isPresent()) {
                    Column columnAnnotation = columnOptional.get();
                    String dataType = columnAnnotation.value().getType();
                    String queryBuilder = "ALTER TABLE " + tableName + " ADD COLUMN" +
                            " " + fieldName + " " + dataType;

                    executeQuery(queryBuilder);
                } else if (enumeratedOptional.isPresent()) {
                    EnumColumn enumeratedAnnotation = enumeratedOptional.get();
                    StringBuilder queryBuilder = new StringBuilder();
                    if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                        queryBuilder.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN");
                        queryBuilder.append(" ").append(fieldName).append(" ").append(FieldType.UINT16.getType());

                        executeQuery(queryBuilder.toString());
                    } else if (enumeratedAnnotation.value() == EnumType.STRING) {
                        queryBuilder.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN");
                        queryBuilder.append(" ").append(fieldName).append(" ").append(FieldType.STRING.getType());

                        executeQuery(queryBuilder.toString());
                    } else {
                        queryBuilder.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN");
                        queryBuilder.append(" ").append(fieldName).append(" ").append(FieldType.LONG.getType());
                    }
                } else if (lobOptional.isPresent()) {
                    StringBuilder queryBuilder = new StringBuilder();
                    queryBuilder.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN");
                    queryBuilder.append(" ").append(fieldName).append(" ").append(FieldType.STRING.getType());

                    executeQuery(queryBuilder.toString());
                } else {
                    throw new FieldInitializationException("Not valid field: " + fieldData);
                }

            }
        }
    }

    private void executeQuery(String query) {
        try (Connection connection = connectionManager.getConnection()) {
            connection.createStatement().execute(query);
            connectionManager.releaseConnection(connection);
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }
}
