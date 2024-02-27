package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.enums.IDTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.altinntech.clicksave.core.CSUtils.generateIntegerId;
import static com.altinntech.clicksave.core.CSUtils.generateLongId;
import static com.altinntech.clicksave.enums.IDTypes.allowedIdTypes;

public class IdsManager {

    private final Map<ClassDataCache, Object> idCache = new HashMap<>();

    private static IdsManager instance;

    private final CSBootstrap bootstrap = CSBootstrap.getInstance();

    private IdsManager() {
    }

    public static IdsManager getInstance() {
        if (instance == null) {
            instance = new IdsManager();
        }
        return instance;
    }

    public Map<ClassDataCache, Object> getIdCache() {
        return idCache;
    }

    public void put(ClassDataCache classDataCache, Object lastId) {
        idCache.put(classDataCache, lastId);
    }

    public Object getLastId(ClassDataCache classDataCache) {
        Object lastId = idCache.get(classDataCache);
        if (lastId == null) {
            adaptiveSync(classDataCache);
            lastId = idCache.get(classDataCache);
        } else {
            Optional<Batching> batchSizeAnnotation = classDataCache.getBatchingAnnotationOptional();
            if (batchSizeAnnotation.isEmpty()) {
                adaptiveSync(classDataCache);
                lastId = idCache.get(classDataCache);
            }
        }
        return lastId;
    }

    void sync(ClassDataCache classDataCache) {
        FieldDataCache idFieldData = classDataCache.getIdField();
        Object refreshedId = null;
        StringBuilder selectIdQuery = new StringBuilder("SELECT ").append(idFieldData.getFieldInTableName()).append(" FROM ").append(classDataCache.getTableName()).append(" ORDER BY ").append(idFieldData.getFieldInTableName()).append(" DESC LIMIT 1");
        try(Connection connection = bootstrap.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectIdQuery.toString())) {
            statement.setMaxRows(1);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    refreshedId = resultSet.getObject(idFieldData.getFieldInTableName());
                    bootstrap.releaseConnection(connection);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (refreshedId != null) {
            idCache.put(classDataCache, refreshedId);
        }
    }

    public void adaptiveSync(ClassDataCache classDataCache) {
        if (classDataCache.getIdField().getType() != IDTypes.UUID.getType()) {
            sync(classDataCache);
        }
    }

    public <ID> ID getNextId(ClassDataCache classDataCache, FieldDataCache idFieldData, ID idType) {
        ID currentId = (ID) getLastId(classDataCache);
        ID nextId = null;

        if (!allowedIdTypes.contains(idType)) {
            throw new IllegalArgumentException("Invalid id type: " + idType);
        }

        if (idType.equals(IDTypes.UUID.getType())) {
            nextId = (ID) UUID.randomUUID();
            idCache.put(classDataCache, nextId);
            return nextId;
        }

        if (idType.equals(IDTypes.INTEGER.getType())) {
            nextId = (ID) generateIntegerId((Integer) currentId);
        } else if (idType.equals(IDTypes.LONG.getType())) {
            nextId = (ID) generateLongId((Long) currentId);
        }

        idCache.put(classDataCache, nextId);
        return nextId;
    }
}
