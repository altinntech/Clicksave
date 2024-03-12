package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.ColumnData;
import com.altinntech.clicksave.core.dto.EmbeddableClassData;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.utils.ClicksaveSequence;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
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
        this(DefaultProperties.fromEnvironment());
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
            Optional<Embedded> embeddedOptional = fieldData.getEmbeddedAnnotation();

            if (fieldData.getFieldType() == null && embeddedOptional.isEmpty()) {
                throw new FieldInitializationException("Not valid field: " + fieldData);
            }

            if (embeddedOptional.isPresent()) {
                EmbeddableClassData embeddableClassData = embeddableClassDataCacheMap.get(fieldData.getType());
                if (embeddableClassData != null) {
                    parseQueryForCreate(primaryKey, embeddableClassData.getFields(), query);
                    continue;
                } else {
                    throw new FieldInitializationException("Embeddable class of field '" + fieldData.getFieldName() + "' not found");
                }
            } else {
                query.append(fieldName).append(" ");
                query.append(fieldData.getFieldType().getType());

                if (fieldData.isPk() || fieldData.isId()) {
                    if (primaryKey.length() == 0) {
                        primaryKey.append(fieldName);
                    } else {
                        primaryKey.append(", ").append(fieldName);
                    }
                }
            }

            query.append(", ");
        }
    }

    private void updateTable(Class<?> clazz) throws FieldInitializationException, ClassCacheNotFoundException {
        ClassDataCache classDataCache = getClassDataCache(clazz);

        String tableName = classDataCache.getTableName();
        info("Check for updates table " + tableName);
        List<ColumnData> tableFieldsFromDB = fetchTableColumns(tableName);
        List<FieldDataCache> fields = classDataCache.getFields();
        checkFields(tableName, fields, tableFieldsFromDB);
    }

    private void checkFields(String tableName, List<FieldDataCache> fieldDataCaches, List<ColumnData> fieldsFromDB) {
        for (FieldDataCache fieldData : fieldDataCaches) {
            String fieldName = fieldData.getFieldInTableName();

            // check for existing
            boolean exists = fieldsFromDB.stream()
                    .anyMatch(columnData -> columnData.getColumnName().equals(fieldName));
            if (!exists && fieldData.getEmbeddedAnnotation().isEmpty()) {
                addColumn(tableName, fieldData);
            } else if (fieldData.getEmbeddedAnnotation().isPresent()) {
                EmbeddableClassData embeddableClassData = embeddableClassDataCacheMap.get(fieldData.getType());
                if (embeddableClassData != null) {
                    checkFields(tableName, embeddableClassData.getFields(), fieldsFromDB);
                }
            }

            //check for types
            if (fieldData.getEmbeddedAnnotation().isEmpty()) {
                String fieldType = fieldData.getFieldType().getType();
                boolean concurrence = fieldsFromDB.stream()
                        .anyMatch(columnData -> columnData.getColumnName().equals(fieldName) &&
                                columnData.getColumnType().equals(fieldType));
                if (exists && !concurrence) {
                    modifyColumn(tableName, fieldData);
                }
            }
        }
    }

    private void addColumn(String tableName, FieldDataCache fieldData) {
        String fieldName = fieldData.getFieldInTableName();
        String dataType = fieldData.getFieldType().getType();
        String queryBuilder = "ALTER TABLE " + tableName + " ADD COLUMN" +
                " " + fieldName + " " + dataType;
        executeQuery(queryBuilder);
        info("Add column '" + fieldName + "' into table '" + tableName + "'");
    }

    private void modifyColumn(String tableName, FieldDataCache fieldData) {
        String fieldName = fieldData.getFieldInTableName();
        String dataType = fieldData.getFieldType().getType();
        String queryBuilder = "ALTER TABLE " + tableName + " MODIFY COLUMN" +
                " " + fieldName + " " + dataType;
        executeQuery(queryBuilder);
        info("Modify column '" + fieldName + "' into table '" + tableName + "'");
    }

    public DefaultProperties getDefaultProperties() {
        return defaultProperties;
    }

    private void executeQuery(String query) {
        try (Connection connection = connectionManager.getConnection()) {
            connection.createStatement().execute(query);
            connectionManager.releaseConnection(connection);
        } catch (SQLException e) {
            error("Query execution error: " + e.getMessage());
        }
    }
}
