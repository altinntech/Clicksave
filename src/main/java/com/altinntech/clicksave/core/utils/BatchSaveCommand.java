package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.core.BatchCollector;
import com.altinntech.clicksave.log.CSLogger;
import lombok.SneakyThrows;

public class BatchSaveCommand implements Runnable {
    
    private final BatchCollector batchCollector;
    
    public BatchSaveCommand(BatchCollector batchCollector) {
        this.batchCollector = batchCollector;
    }

    @SneakyThrows
    @Override
    public void run() {
        if (batchCollector.isNotEmpty()) {
            CSLogger.debug("Batch", "Saving batch");
            batchCollector.saveAndFlushAll();
        }
    }
}
