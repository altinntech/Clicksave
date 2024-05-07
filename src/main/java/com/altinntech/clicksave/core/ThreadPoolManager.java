package com.altinntech.clicksave.core;

import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.*;

import static com.altinntech.clicksave.log.CSLogger.*;

public class ThreadPoolManager {

    private final ThreadPoolExecutor executor;

    public ThreadPoolManager() {
        int processors = Runtime.getRuntime().availableProcessors();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        this.executor = new ThreadPoolExecutor(
                processors,
                processors,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                handler);
        info("ThreadPoolManager started with " + processors + " processors");
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
            if (!executor.awaitTermination(12, TimeUnit.SECONDS)) {
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
}
