package com.altinntech.clicksave.core;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.utils.ClicksaveSequence;
import com.altinntech.clicksave.enums.IDTypes;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.altinntech.clicksave.core.CSUtils.*;
import static com.altinntech.clicksave.enums.IDTypes.allowedIdTypes;

public class IdsManager {

    private final Map<ClassDataCache, Object> idCache = new HashMap<>();

    private CHRepository repository;
    private final ConnectionManager connectionManager;
    private boolean isInitialized = false;

    IdsManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setRepository(CHRepository repository) {
        this.repository = repository;
        this.isInitialized = true;
    }

    public Map<ClassDataCache, Object> getIdCache() {
        return idCache;
    }

    public void put(ClassDataCache classDataCache, Object lastId) {
        idCache.put(classDataCache, lastId);
    }

    public synchronized Object getLastId(ClassDataCache classDataCache) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        Object lastId = idCache.get(classDataCache);
        if (lastId == null) {
            adaptiveSync(classDataCache);
        } else {
            Optional<Batching> batchAnnotation = classDataCache.getBatchingAnnotationOptional();
            if (batchAnnotation.isEmpty()) {
                adaptiveSync(classDataCache);
            }
        }
        lastId = idCache.get(classDataCache);
        return lastId;
    }

    synchronized void sync(ClassDataCache classDataCache) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        while (!isInitialized) {
            Thread.yield();
        }
        Object refreshedId = null;
        Properties properties = new Properties();
        properties.setProperty("table_name", classDataCache.getTableName());
        Optional<ClicksaveSequence> lastLockRecord = repository.findLast(ClicksaveSequence.class, properties);
        if (lastLockRecord.isPresent() && lastLockRecord.get().getIsLocked() == 1) {
            refreshedId = lastLockRecord.get().getEndLockId();
            idCache.put(classDataCache, refreshedId);
            ClicksaveSequence unlockMarker =  createLockRecord(classDataCache, -1L, -1L, false);
            repository.save(unlockMarker, unlockMarker.getTimestamp().getClass());
            return;
        }
        FieldDataCache idFieldData = classDataCache.getIdField();
        StringBuilder selectIdQuery = new StringBuilder("SELECT ").append(idFieldData.getFieldInTableName()).append(" FROM ").append(classDataCache.getTableName()).append(" ORDER BY ").append(idFieldData.getFieldInTableName()).append(" DESC LIMIT 1");
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectIdQuery.toString())) {
            statement.setMaxRows(1);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    refreshedId = resultSet.getObject(idFieldData.getFieldInTableName());
                    connectionManager.releaseConnection(connection);
                }
            }
        }

        if (refreshedId != null) {
            idCache.put(classDataCache, refreshedId);
        }
    }

    public synchronized void adaptiveSync(ClassDataCache classDataCache) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        if (classDataCache.getIdField().getType() != IDTypes.UUID.getType()) {
            sync(classDataCache);
        }
    }

    public synchronized <ID> ID getNextId(ClassDataCache classDataCache, FieldDataCache idFieldData, ID idType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
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
        } else {
            throw new IllegalArgumentException("Invalid id type: " + idType);
        }

        idCache.put(classDataCache, nextId);
        return nextId;
    }

    public synchronized  <ID> void lockIds(ClassDataCache classDataCache, int range, ID idType) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        while (!isInitialized) {
            Thread.yield();
        }
        ID startId = (ID) getLastId(classDataCache);
        ID endId = null;

        if (idType.equals(IDTypes.UUID.getType())) {
            return;
        }
        if (!allowedIdTypes.contains(idType)) {
            throw new IllegalArgumentException("Invalid id type: " + idType);
        }

        if (idType.equals(IDTypes.INTEGER.getType())) {
            endId = (ID) getRangedIntegerId((Integer) startId, range);
        } else if (idType.equals(IDTypes.LONG.getType())) {
            endId = (ID) getRangedLongId((Long) startId, range);
        }

        ClicksaveSequence lockRecord = createLockRecord(classDataCache, (Long) startId, (Long) endId, true);
        repository.save(lockRecord, lockRecord.getTimestamp().getClass());
    }

    private synchronized static <ID> ClicksaveSequence createLockRecord(ClassDataCache classDataCache, Long startId, Long endId, boolean isLocked) {
        ClicksaveSequence lockRecord = new ClicksaveSequence();
        lockRecord.setTimestamp(System.nanoTime());
        lockRecord.setStartLockId(startId);
        lockRecord.setEndLockId(endId);
        lockRecord.setTableName(classDataCache.getTableName());
        if (isLocked)
            lockRecord.setIsLocked(1);
        else
            lockRecord.setIsLocked(0);
        return lockRecord;
    }
}
