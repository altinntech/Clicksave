package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.BatchedQueryData;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.altinntech.clicksave.log.CSLogger.debug;
import static com.altinntech.clicksave.log.CSLogger.info;

/**
 * The {@code BatchCollector} class is responsible for collecting batches of queries.
 * It accumulates queries and sends them for processing when a threshold is reached.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class BatchCollector {

    /**
     * The map to store batches of queries.
     */
    private final Map<BatchedQueryData, List<List<Object>>> batches = new HashMap<>();

    private final IdsManager idsManager = IdsManager.getInstance();

    /**
     * The bootstrap instance for database connectivity.
     */
    private final CSBootstrap bootstrap;

    private static BatchCollector instance;

    public static BatchCollector getInstance() {
        if (instance == null) {
            instance = new BatchCollector();
        }
        return instance;
    }

    /**
     * Instantiates a new Batch collector.
     */
    private BatchCollector() {
        this.bootstrap = CSBootstrap.getInstance();
    }

    /**
     * Adds query data to the batch.
     *
     * @param batchQueryData the batch query data
     * @param fieldsData     the field data
     */
    public void put(BatchedQueryData batchQueryData, List<Object> fieldsData) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
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
    public void saveAndFlush(ClassDataCache classDataCache) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
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
    public void saveAndFlush(BatchedQueryData queryMeta, List<List<Object>> batch) throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        String query = queryMeta.getQuery();
        try(Connection connection = bootstrap.getConnection()) {
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
                bootstrap.releaseConnection(connection);
                batch.clear();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        idsManager.sync(queryMeta.getClassDataCache());
        debug(query + " batch saved");
    }

    /**
     * Saves and flushes all batches.
     */
    public void saveAndFlushAll() throws SQLException, ClassCacheNotFoundException, IllegalAccessException {
        for (Map.Entry<BatchedQueryData, List<List<Object>>> entry : batches.entrySet()) {
            BatchedQueryData batchedQueryData = entry.getKey();
            List<List<Object>> batch = entry.getValue();
            if (batch.isEmpty())
                continue;
            saveAndFlush(batchedQueryData, batch);
        }
        info("All batches saved");
    }
}
