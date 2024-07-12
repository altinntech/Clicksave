package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.metrics.dto.ThreadPoolManagerMetrics;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.concurrent.*;

import static com.altinntech.clicksave.log.CSLogger.*;

public class ThreadPoolManager {

    private final ThreadPoolExecutor executor;

    public ThreadPoolManager() {
        int processors = Runtime.getRuntime().availableProcessors();
        executor = initThreadPool(processors, 1000);
    }

    public ThreadPoolManager(DefaultProperties properties) {
        int processors = Integer.parseInt(properties.getThreadManagerMaxProcessors());
        if (processors <= 0) {
            processors = Runtime.getRuntime().availableProcessors();
        }
        executor = initThreadPool(processors, Integer.parseInt(properties.getThreadManagerMaxQueueSize()));
    }

    private ThreadPoolExecutor initThreadPool(int processors, int capacity) {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                processors,
                processors,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(capacity),
                handler);
        info("ThreadPoolManager started with " + processors + " processors");
        return executor;
    }

    public <T> Future<T> saveAsync(Object[] arguments, Class<T> entityIdType, CHRepository repository) throws InterruptedException {
        return (Future<T>) executor.submit(() -> {
            try {
                return repository.save(arguments[0], entityIdType);
            } catch (SQLException | ClassCacheNotFoundException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error saving entity", e);
            }
        });
    }

    public void shutdown() {
        info("ThreadPoolManager shutdown process initiated...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                warn("ThreadPoolManager shutdown process exceeded the waiting time. Attempt to force shutdown!");
                executor.shutdownNow();
            }
            info("ThreadPoolManager shutdown process completed successfully");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            warn("ThreadPoolManager stopped emergency!");
            Thread.currentThread().interrupt();
        }
    }

    public void waitForCompletion() {
        while (executor.getActiveCount() > 0) {
            Thread.yield();
        }
    }

    public ThreadPoolManagerMetrics getMetrics() {
        ThreadPoolManagerMetrics metrics = new ThreadPoolManagerMetrics();
        metrics.setIsShutdown(executor.isShutdown());
        return metrics;
    }
}
