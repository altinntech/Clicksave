package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.core.dto.BatchedQueryData;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.IDTypes;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.exceptions.FieldInitializationException;
import com.altinntech.clicksave.exceptions.NotImplementationException;
import com.altinntech.clicksave.interfaces.EnumId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.altinntech.clicksave.core.CSUtils.*;
import static com.altinntech.clicksave.enums.IDTypes.allowedIdTypes;
import static com.altinntech.clicksave.log.CSLogger.error;

/**
 * The {@code CHRepository} class represents a service repository that facilitates communication between the ORM and the database.
 * It handles basic CRUD operations.
 *
 * <p>This class is annotated with {@code @Component} for Spring dependency injection.</p>
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
@Component
public class CHRepository {

    /**
     * The CSBootstrap instance for database connectivity.
     */
    private final CSBootstrap CSBootstrap;

    /**
     * The BatchCollector instance for managing batches of queries.
     */
    private final BatchCollector batchCollector;

    private final IdsManager idsManager = IdsManager.getInstance();

    /**
     * Instantiates a new ClickHouse repository.
     *
     * @param CSBootstrap the CSBootstrap
     */
    @Autowired
    public CHRepository(CSBootstrap CSBootstrap) {
        this.CSBootstrap = CSBootstrap;
        this.batchCollector = CSBootstrap.getBatchCollector();
    }

    /**
     * Saves an entity to the database.
     *
     * @param <T>    the type parameter
     * @param entity the entity to save
     * @return the saved entity
     * @throws FieldInitializationException if there is an error with field initialization
     * @throws ClassCacheNotFoundException if the class cache is not found
     * @throws IllegalAccessException      if illegal access occurs
     * @throws SQLException                 if a SQL exception occurs
     */
    public <T, ID> T save(T entity, ID idType) throws FieldInitializationException, ClassCacheNotFoundException, IllegalAccessException, SQLException {
        Class<?> entityClass = entity.getClass();
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        FieldDataCache idFieldData = classDataCache.getIdField();
        Field idField = idFieldData.getField();

        // check for update
        idField.setAccessible(true);
        ID id = (ID) idFieldData.getField().get(entity);
        if (id != null && entityExists(entityClass, id)) {
            return update(entity, classDataCache, idFieldData, id);
        }

        String tableName = classDataCache.getTableName();
        StringBuilder insertQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder valuesPlaceholder = new StringBuilder(" VALUES (");

        List<FieldDataCache> fields = classDataCache.getFields();
        List<Object> fieldValues = new ArrayList<>();

        for (FieldDataCache fieldData : fields) {
            String columnName = fieldData.getFieldInTableName();
            Field field = fieldData.getField();
            Optional<Column> columnOptional = fieldData.getColumnAnnotation();
            Optional<EnumColumn> enumeratedOptional = fieldData.getEnumColumnAnnotation();

            if (columnOptional.isPresent()) {
                Column columnAnnotation = columnOptional.get();
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(entity);
                    if (value == null && columnAnnotation.id()) {
                        value = idsManager.getNextId(classDataCache, fieldData, idType);
                        setFieldValue(entity, field, value, fieldData);
                    }
                } catch (IllegalAccessException e) {
                    error(e.getMessage());
                }
                insertQuery.append(columnName).append(", ");
                valuesPlaceholder.append("?, ");
                fieldValues.add(value);
            } else if (enumeratedOptional.isPresent()) {
                EnumColumn enumeratedAnnotation = enumeratedOptional.get();
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(entity);
                    if (enumeratedAnnotation.value() == EnumType.STRING) {
                        value = value.toString();
                    } else if (enumeratedAnnotation.value() == EnumType.ORDINAL) {
                        Enum<?> enumValue = (Enum<?>) value;
                        value = enumValue.ordinal();
                    } else {
                        EnumId enumValue = (EnumId) value;
                        value = enumValue.getId();
                    }
                } catch (IllegalAccessException e) {
                    error(e.getMessage());
                }
                insertQuery.append(columnName).append(", ");
                valuesPlaceholder.append("?, ");
                fieldValues.add(value);
            } else {
                throw new FieldInitializationException();
            }
        }

        insertQuery.delete(insertQuery.length() - 2, insertQuery.length()).append(")");
        valuesPlaceholder.delete(valuesPlaceholder.length() - 2, valuesPlaceholder.length()).append(")");

        String query = insertQuery + valuesPlaceholder.toString();

        Optional<Batching> batchSizeAnnotation = classDataCache.getBatchingAnnotationOptional();
        if (batchSizeAnnotation.isPresent()) {
            BatchedQueryData batchedQuery = new BatchedQueryData(query, classDataCache);
            batchCollector.put(batchedQuery, fieldValues);
            return entity;
        }

        try(Connection connection = CSBootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < fieldValues.size(); i++) {
                statement.setObject(i + 1, fieldValues.get(i));
            }
            statement.addBatch();
            statement.executeBatch();
            CSBootstrap.releaseConnection(connection);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return entity;
    }

    /**
     * Saves an entity to the database.
     *
     * @param <T>    the type parameter
     * @param entity the entity to update
     * @param classDataCache the classData of the entity
     * @param idFieldData the fieldData of the id filed
     * @param id of the entity
     * @return the updated entity
     * @throws IllegalAccessException      if illegal access occurs
     * @throws SQLException                 if a SQL exception occurs
     */
    // todo: refactor
    private <T, ID> T update(T entity, ClassDataCache classDataCache, FieldDataCache idFieldData, ID id) throws IllegalAccessException, SQLException {
        String tableName = classDataCache.getTableName();
        StringBuilder updateQuery = new StringBuilder("ALTER TABLE ").append(tableName).append(" UPDATE ");
        batchCollector.saveAndFlush(classDataCache);

        List<FieldDataCache> fields = classDataCache.getFields();
        for (FieldDataCache fieldData : fields) {
            String columnName = fieldData.getFieldInTableName();
            Field field = fieldData.getField();
            Optional<Column> columnOptional = fieldData.getColumnAnnotation();
            Optional<EnumColumn> enumeratedOptional = fieldData.getEnumColumnAnnotation();

            if (columnOptional.isPresent()) {
                Column columnAnnotation = columnOptional.get();
                if (!columnAnnotation.id() && !columnAnnotation.primaryKey()) {
                    field.setAccessible(true);
                    updateQuery.append(columnName).append(" = ").append("'" + field.get(entity) + "'").append(", ");
                }
            } else if (enumeratedOptional.isPresent()) {
                EnumColumn enumColumnAnnotation = enumeratedOptional.get();
                field.setAccessible(true);
                Object value = field.get(entity);
                if (enumColumnAnnotation.value() == EnumType.STRING) {
                    value = value.toString();
                } else if (enumColumnAnnotation.value() == EnumType.ORDINAL) {
                    Enum<?> enumValue = (Enum<?>) value;
                    value = enumValue.ordinal();
                } else {
                    EnumId enumValue = (EnumId) value;
                    value = enumValue.getId();
                }
                updateQuery.append(columnName).append(" = ").append("'" + value + "'").append(", ");
            }
        }

        updateQuery.delete(updateQuery.length() - 2, updateQuery.length()).append(" WHERE ")
                .append(idFieldData.getFieldInTableName()).append(" = ").append("'" + id + "'");

        try(Connection connection = CSBootstrap.getConnection();
            Statement statement = connection.createStatement()) {
            int rowAffected = statement.executeUpdate(updateQuery.toString());
            CSBootstrap.releaseConnection(connection);
        } catch (SQLException e) {
            error(e.getMessage());
        }

        return entity;
    }

    /**
     * Finds an entity by its ID.
     *
     * @param <T>         the type parameter
     * @param entityClass the entity class
     * @param id          the ID of the entity
     * @return the entity if found, otherwise null
     * @throws ClassCacheNotFoundException if the class cache is not found
     */
    public <T, ID> T findById(Class<T> entityClass, ID id) throws ClassCacheNotFoundException {
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        String tableName = classDataCache.getTableName();
        StringBuilder selectQuery = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");
        batchCollector.saveAndFlush(classDataCache);

        FieldDataCache idFieldCache = classDataCache.getIdField();
        String idFieldName = idFieldCache.getFieldInTableName();
        selectQuery.append(idFieldName).append(" = ?");

        try(Connection connection = CSBootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectQuery.toString())) {
            statement.setMaxRows(1);
            statement.setObject(1, id);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    T entity = createEntityFromResultSet(entityClass, resultSet, classDataCache);
                    CSBootstrap.releaseConnection(connection);
                    return entity;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Retrieves all entities from the database.
     *
     * @param <T>         the type parameter
     * @param entityClass the entity class
     * @return the list of all entities
     * @throws ClassCacheNotFoundException if the class cache is not found
     * @throws SQLException                if a SQL exception occurs
     */
    public <T> List<T> findAll(Class<T> entityClass) throws ClassCacheNotFoundException, SQLException {
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        String tableName = classDataCache.getTableName();
        String selectQuery = "SELECT * FROM " + tableName;
        batchCollector.saveAndFlush(classDataCache);

        try(Connection connection = CSBootstrap.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery)) {
            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                T entity = createEntityFromResultSet(entityClass, resultSet, classDataCache);
                entities.add(entity);
            }
            CSBootstrap.releaseConnection(connection);
            return entities;
        }
    }

    /**
     * Deletes all records from the database table associated with the specified entity class.
     *
     * @param entityClass the entity class
     * @throws ClassCacheNotFoundException if the class cache is not found
     */
    public void deleteAll(Class<?> entityClass) throws ClassCacheNotFoundException {
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        String tableName = classDataCache.getTableName();
        StringBuilder deleteQuery = new StringBuilder("ALTER TABLE ").append(tableName).append(" DELETE WHERE true");
        batchCollector.saveAndFlush(classDataCache);

        try(Connection connection = CSBootstrap.getConnection();
            Statement statement = connection.createStatement()) {
            int rowAffected = statement.executeUpdate(deleteQuery.toString());
            CSBootstrap.releaseConnection(connection);
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    /**
     * Deletes the specified entity from the database.
     *
     * @param <T>    the type parameter
     * @param entity the entity to delete
     * @throws ClassCacheNotFoundException if the class cache is not found
     * @throws IllegalAccessException      if there is illegal access to the entity's fields
     */
    public <T> void delete(T entity) throws ClassCacheNotFoundException, IllegalAccessException {
        Class<?> entityClass = entity.getClass();
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        FieldDataCache idFieldData = classDataCache.getIdField();
        Field idField = idFieldData.getField();
        idField.setAccessible(true);
        batchCollector.saveAndFlush(classDataCache);

        StringBuilder deleteQuery = new StringBuilder("DELETE FROM ").append(classDataCache.getTableName()).append(" WHERE ");
        String columnName = idFieldData.getFieldInTableName();
        Object id = idField.get(entity);
        deleteQuery.append(columnName).append(" = ").append("'").append(id).append("'");

        try(Connection connection = CSBootstrap.getConnection();
            Statement statement = connection.createStatement()) {
            int rowAffected = statement.executeUpdate(deleteQuery.toString());
            idField.set(entity, null);
            CSBootstrap.releaseConnection(connection);
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    /**
     * Checks if an entity with the specified ID exists in the database.
     *
     * @param <T>         the type parameter
     * @param entityClass the entity class
     * @param id          the ID of the entity
     * @return true if the entity exists, false otherwise
     * @throws ClassCacheNotFoundException if the class cache is not found
     */
    public <T, ID> boolean entityExists(Class<T> entityClass, ID id) throws ClassCacheNotFoundException {
        ClassDataCache classDataCache = CSBootstrap.getClassDataCache(entityClass);
        batchCollector.saveAndFlush(classDataCache);
        String tableName = classDataCache.getTableName();
        FieldDataCache idFieldCache = classDataCache.getIdField();
        StringBuilder selectQuery = new StringBuilder("SELECT " + idFieldCache.getFieldInTableName() + " FROM ").append(tableName).append(" WHERE ");
        String idFieldName = idFieldCache.getFieldInTableName();
        selectQuery.append(idFieldName).append(" = ?");

        try(Connection connection = CSBootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectQuery.toString())) {
            statement.setMaxRows(1);
            statement.setObject(1, id);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CSBootstrap.releaseConnection(connection);
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
