package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.BatchedQueryData;
import com.altinntech.clicksave.core.dto.ClassDataCache;
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

@Component
public class BatchCollector {

    private final Map<BatchedQueryData, List<List<Object>>> batches = new HashMap<>();
    private final CSBootstrap bootstrap;

    public BatchCollector(CSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void put(BatchedQueryData batchQueryData, List<Object> fieldsData) {
        if (!batches.containsKey(batchQueryData)) {
            batches.put(batchQueryData, new ArrayList<>());
        }

        List<List<Object>> batch = batches.get(batchQueryData);
        batch.add(fieldsData);

        if (batch.size() >= batchQueryData.getClassDataCache().getBatchingAnnotation().batchSize()) {
            saveAndFlush(batchQueryData.getQuery(), batch);
        }
    }

    public void saveAndFlush(ClassDataCache classDataCache) {
        for (Map.Entry<BatchedQueryData, List<List<Object>>> entry : batches.entrySet()) {
            BatchedQueryData batchedQueryData = entry.getKey();
            ClassDataCache cacheInMap = batchedQueryData.getClassDataCache();
            if (classDataCache.equals(cacheInMap)) {
                List<List<Object>> batch = entry.getValue();
                if (batch.isEmpty())
                    return;
                saveAndFlush(batchedQueryData.getQuery(), batch);
                return;
            }
        }
    }

    // todo: add log
    public void saveAndFlush(String query, List<List<Object>> batch) {
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
        debug(query + " batch saved");
    }

    public void saveAndFlushAll() {
        for (Map.Entry<BatchedQueryData, List<List<Object>>> entry : batches.entrySet()) {
            BatchedQueryData batchedQueryData = entry.getKey();
            List<List<Object>> batch = entry.getValue();
            if (batch.isEmpty())
                continue;
            saveAndFlush(batchedQueryData.getQuery(), batch);
        }
        info("All batches saved");
    }
}
