package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.BatchedQueryData;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.utils.BatchSaveCommand;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.altinntech.clicksave.log.CSLogger.debug;
import static com.altinntech.clicksave.log.CSLogger.info;

/**
 * The {@code BatchCollector} class is responsible for collecting batches of queries.
 * It accumulates queries and sends them for processing when a threshold is reached.
 *
 * @author Fyodor Plotnikov
 */
public class BatchCollector {

    /**
     * The map to store batches of queries.
     */
    private final ConcurrentHashMap<BatchedQueryData, List<List<Object>>> batches = new ConcurrentHashMap<>();

    private final IdsManager idsManager;
    private final ConnectionManager connectionManager;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Instantiates a new Batch collector.
     */
    private BatchCollector(IdsManager idsManager, ConnectionManager connectionManager) {
        this.idsManager = idsManager;
        this.connectionManager = connectionManager;
    }

    public static BatchCollector create(IdsManager idsManager, ConnectionManager connectionManager, DefaultProperties properties) {
        BatchCollector batchCollector = new BatchCollector(idsManager, connectionManager);
        long batchSaveRate = Long.parseLong(properties.getBatchSaveRate());
        if (batchSaveRate > 0) {
            batchCollector.scheduler.scheduleAtFixedRate(new BatchSaveCommand(batchCollector), 2000, batchSaveRate, TimeUnit.MILLISECONDS);
            info("Batch save scheduler status: active");
            debug("Batch save scheduler rate: every " + batchSaveRate + " ms");
        } else {
            info("Batch save scheduler status: inactive");
        }
        return batchCollector;
    }

    public synchronized void dispose() {
        scheduler.shutdown();
    }

    public synchronized boolean isNotEmpty() {
        for (List<List<Object>> list : batches.values()) {
            if (!list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds query data to the batch.
     *
     * @param batchQueryData the batch query data
     * @param fieldsData     the field data
     */
    public synchronized void put(BatchedQueryData batchQueryData, List<Object> fieldsData) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        ClassDataCache classDataCache = batchQueryData.getClassDataCache();
        if (!batches.containsKey(batchQueryData)) {
            batches.put(batchQueryData, new ArrayList<>());
        }

        List<List<Object>> batch = batches.get(batchQueryData);
        batch.add(fieldsData);

        if (batch.size() >= classDataCache.getBatchingAnnotation().batchSize()) {
            saveAndFlush(batchQueryData, batch);
        } else if (batch.size() == 1) {
            idsManager.lockIds(classDataCache, classDataCache.getBatchingAnnotation().batchSize(), classDataCache.getIdField().getType());
        }
    }

    /**
     * Saves and flushes the batch for a specific class data cache.
     *
     * @param classDataCache the class data cache
     */
    public synchronized void saveAndFlush(ClassDataCache classDataCache) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<BatchedQueryData, List<List<Object>>> entry : batches.entrySet()) {
            BatchedQueryData batchedQueryData = entry.getKey();
            ClassDataCache cacheInMap = batchedQueryData.getClassDataCache();
            if (classDataCache.equals(cacheInMap)) {
                List<List<Object>> batch = entry.getValue();
                if (batch.isEmpty())
                    return;
                saveAndFlush(batchedQueryData, batch);
                return;
            }
        }
    }

    /**
     * Saves and flushes the batch.
     *
     * @param queryMeta the query
     * @param batch the batch
     */
    public synchronized void saveAndFlush(BatchedQueryData queryMeta, List<List<Object>> batch) throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        String query = queryMeta.getQuery();
        int size = batch.size();
        try(Connection connection = connectionManager.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            try {
                for (List<Object> queryData : batch) {
                    for (int i = 0; i < queryData.size(); i++) {
                        statement.setObject(i + 1, queryData.get(i));
                    }
                    statement.addBatch();
                }

                statement.executeBatch();
            } finally {
                if (statement != null) {
                    statement.close();
                }
                connectionManager.releaseConnection(connection);
                batch.clear();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        idsManager.adaptiveSync(queryMeta.getClassDataCache());
        debug("Batch", query + " saved " + size);
    }

    /**
     * Saves and flushes all batches.
     */
    public synchronized void saveAndFlushAll() throws SQLException, ClassCacheNotFoundException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<BatchedQueryData, List<List<Object>>> entry : batches.entrySet()) {
            BatchedQueryData batchedQueryData = entry.getKey();
            List<List<Object>> batch = entry.getValue();
            if (batch.isEmpty())
                continue;
            saveAndFlush(batchedQueryData, batch);
        }
        debug("All batches saved");
    }
}
