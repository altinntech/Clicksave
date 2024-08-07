package com.altinntech.clicksave.metrics;

import com.altinntech.clicksave.core.ConnectionManager;
import com.altinntech.clicksave.core.SyncManager;
import com.altinntech.clicksave.core.ThreadPoolManager;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.metrics.dto.base.MetricsLog;
import lombok.Data;

@Data
public class MonitoringService {

    DefaultProperties properties;
    ConnectionManager connectionManager;
    ThreadPoolManager threadPoolManager;
    SyncManager syncManager;

    public MonitoringService(ConnectionManager connectionManager, ThreadPoolManager threadPoolManager, SyncManager syncManager, DefaultProperties properties) {
        this.connectionManager = connectionManager;
        this.properties = properties;
        this.threadPoolManager = threadPoolManager;
        this.syncManager = syncManager;
    }

    public MetricsLog getMetricsLog() {
        MetricsLog metricsLog = new MetricsLog();
        metricsLog.setProperties(properties.toMap());
        metricsLog.setConnectionManagerMetrics(connectionManager.getMetrics());
        metricsLog.setThreadPoolManagerMetrics(threadPoolManager.getMetrics());
        return metricsLog;
    }
}
