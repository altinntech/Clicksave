package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.BatchedQueryData;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.utils.BatchSaveCommand;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.enums.Metrics;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.ClicksaveMetrics;
import com.altinntech.clicksave.interfaces.Disposable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.altinntech.clicksave.log.CSLogger.*;

/**
 * The {@code BatchCollector} class is responsible for collecting batches of queries.
 * It accumulates queries and sends them for processing when a threshold is reached.
 *
 * @author Fyodor Plotnikov
 */
public class BatchCollector implements Disposable {

    /**
     * The map to store batches of queries.
     */
    private final ConcurrentHashMap<BatchedQueryData, List<List<Object>>> batches = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final String failedBatchSavePath;

    private final IdsManager idsManager;
    private final ConnectionManager connectionManager;
    private final ClicksaveMetrics metrics;

    /**
     * Instantiates a new Batch collector.
     */
    private BatchCollector(IdsManager idsManager, ConnectionManager connectionManager, ClicksaveMetrics metrics, DefaultProperties properties) {
        this.idsManager = idsManager;
        this.connectionManager = connectionManager;
        this.failedBatchSavePath = properties.getFailedBatchSavePath();
        this.metrics = metrics;
    }

    public static BatchCollector create(IdsManager idsManager, ConnectionManager connectionManager, DefaultProperties properties, ClicksaveMetrics metrics) {
        BatchCollector batchCollector = new BatchCollector(idsManager, connectionManager, metrics, properties);
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

    @Override
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
        int maxRetries = 3;
        int attempt = 0;
        boolean success = false;

        while (attempt < maxRetries && !success) {
            attempt++;
            try (Connection connection = connectionManager.getConnection()) {

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (List<Object> queryData : batch) {
                        for (int i = 0; i < queryData.size(); i++) {
                            statement.setObject(i + 1, queryData.get(i));
                        }
                        statement.addBatch();
                    }

                    statement.executeBatch();
                    success = true;
                    metrics.incrementCounter(Metrics.BATCH_SUCCESS.getMetricsName());
                } catch (SQLException e) {
                    if (attempt == maxRetries) {
                        saveFailedBatchToCsv(queryMeta.getClassDataCache(), batch, failedBatchSavePath);
                        error("Failed to execute batch with error '" + e.getMessage() + "'", e);
                        error("Failed to execute batch after " + maxRetries + " attempts", this.getClass());
                        metrics.incrementCounter(Metrics.BATCH_FAIL.getMetricsName());
                    } else {
                        debug("<BatchCollector>", "Save attempt " + attempt + " failed, retrying...");
                        Thread.sleep(1000);
                    }
                } finally {
                    connectionManager.releaseConnection(connection);
                }
            } catch (SQLException | InterruptedException e) {
                if (attempt == maxRetries) {
                    error("Failed to get connection after " + maxRetries + " attempts", this.getClass());
                }
            }
        }

        batch.clear();

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

    public void saveFailedBatchToCsv(ClassDataCache classDataCache, List<List<Object>> failedBatch, String filePath) {
        String tableName = classDataCache.getTableName();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = tableName + "_" + timestamp + ".csv";

        File file = Paths.get(filePath, fileName).toFile();
        file.getParentFile().mkdirs();

        try (FileWriter csvWriter = new FileWriter(file)) {
            for (List<Object> rowData : failedBatch) {
                List<String> row = rowData.stream()
                        .map(this::convertToCsvSafeString)
                        .collect(Collectors.toList());
                csvWriter.append(String.join(",", row));
                csvWriter.append("\n");
            }

            csvWriter.flush();
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private String convertToCsvSafeString(Object value) {
        if (value == null) {
            return "";
        }

        String stringValue = value.toString();

        if (stringValue.contains(",") || stringValue.contains("\"") || stringValue.contains("\n")) {
            stringValue = stringValue.replace("\"", "\"\"");
            return "\"" + stringValue + "\"";
        }

        return stringValue;
    }
}
