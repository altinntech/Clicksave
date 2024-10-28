package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.interfaces.Observer;
import com.altinntech.clicksave.interfaces.PropertyEnvironment;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code DefaultProperties} class provides default properties for configuring the application.
 * It retrieves properties from the environment and provides default values if not specified.
 *
 * @author Fyodor Plotnikov
 */

@Getter
public class DefaultProperties {

    private final List<Observer> observers = new ArrayList<>();

    private String url;
    private String username;
    private String password;
    private String initialConnectionsPoolSize;
    private String connectionsPoolSizeRefillThreshold;
    private String maxConnectionPoolSize;
    private String allowConnectionsPoolExpansion;
    private String rootPackageToScan;
    private String testEnv;
    private String batchSaveRate;
    private String threadManagerMaxProcessors;
    private String threadManagerMaxQueueSize;
    @Getter
    private String useSyncFeatures;
    @Getter
    private String syncHostPort;
    @Getter
    private String syncRemoteHosts;
    @Getter
    private String syncConnectionRetryTimeout;
    @Getter
    private String migrationsDirectoryPath;
    @Getter
    private String failedBatchSavePath;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("url", this.url);
        map.put("initialConnectionsPoolSize", this.initialConnectionsPoolSize);
        map.put("connectionsPoolSizeRefillThreshold", this.connectionsPoolSizeRefillThreshold);
        map.put("maxConnectionPoolSize", this.maxConnectionPoolSize);
        map.put("allowConnectionsPoolExpansion", this.allowConnectionsPoolExpansion);
        map.put("rootPackageToScan", this.rootPackageToScan);
        map.put("testEnv", this.testEnv);
        map.put("batchSaveRate", this.batchSaveRate);
        map.put("threadManagerMaxProcessors", this.threadManagerMaxProcessors);
        map.put("threadManagerMaxQueueSize", this.threadManagerMaxQueueSize);
        map.put("syncHostPort", this.syncHostPort);
        map.put("syncRemoteHosts", this.syncRemoteHosts);
        map.put("useSyncFeatures", this.useSyncFeatures);
        map.put("syncConnectionRetryTimeout", this.syncConnectionRetryTimeout);
        map.put("migrationsDirectoryPath", this.migrationsDirectoryPath);
        map.put("failedBatchSavePath", this.failedBatchSavePath);
        return map;
    }

    public static DefaultProperties fromPropertyFile() {
        PropertyReader propertyReader = PropertyReader.getInstance();
        return getProperties(propertyReader);
    }

    public static DefaultProperties fromEnvironment(Environment environment) {
        SpringEnvironment springEnvironment = new SpringEnvironment(environment);
        return getProperties(springEnvironment);
    }

    private static DefaultProperties getProperties(PropertyEnvironment propertyEnvironment) {
        DefaultProperties defaultProperties = new DefaultProperties();
        defaultProperties.url = propertyEnvironment.getProperty("clicksave.connection.datasource.url", "");
        defaultProperties.username = propertyEnvironment.getProperty("clicksave.connection.datasource.username");
        defaultProperties.password = propertyEnvironment.getProperty("clicksave.connection.datasource.password");
        defaultProperties.initialConnectionsPoolSize = propertyEnvironment.getProperty("clicksave.connection.pool.initial-size", "40");
        defaultProperties.connectionsPoolSizeRefillThreshold = propertyEnvironment.getProperty("clicksave.connection.pool.refill-threshold", "5");
        defaultProperties.maxConnectionPoolSize = propertyEnvironment.getProperty("clicksave.connection.pool.max-size", "50");
        defaultProperties.allowConnectionsPoolExpansion = propertyEnvironment.getProperty("clicksave.connection.pool.allow-expansion", "true");
        defaultProperties.rootPackageToScan = propertyEnvironment.getProperty("clicksave.core.root-package", "");
        defaultProperties.testEnv = propertyEnvironment.getProperty("clicksave.test-env", "false");
        defaultProperties.batchSaveRate = propertyEnvironment.getProperty("clicksave.core.batch-save-rate", "1200");
        defaultProperties.threadManagerMaxProcessors = propertyEnvironment.getProperty("clicksave.core.thread-manager.max-processors", "-1");
        defaultProperties.threadManagerMaxQueueSize = propertyEnvironment.getProperty("clicksave.core.core.thread-manager.max-queue-size", "1000");
        defaultProperties.syncHostPort = propertyEnvironment.getProperty("clicksave.sync.host.port", "");
        defaultProperties.syncRemoteHosts = propertyEnvironment.getProperty("clicksave.sync.remote.hosts", "");
        defaultProperties.useSyncFeatures = propertyEnvironment.getProperty("clicksave.sync.use-sync-features", "false");
        defaultProperties.syncConnectionRetryTimeout = propertyEnvironment.getProperty("clicksave.sync.connection-retry-timeout", "0");
        defaultProperties.migrationsDirectoryPath = propertyEnvironment.getProperty("clicksave.utils.migrations-directory-path", "");
        defaultProperties.failedBatchSavePath = propertyEnvironment.getProperty("clicksave.utils.failed-batch-directory-path", "");
        return defaultProperties;
    }

    public boolean validate() {
        return !url.isEmpty() && password != null && username != null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws SQLException {
        this.url = url;
        notifyObservers();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws SQLException {
        this.username = username;
        notifyObservers();
    }

    public void setPassword(String password) throws SQLException {
        this.password = password;
        notifyObservers();
    }

    public String getPassword() {
        return this.password;
    }

    public void setInitialConnectionsPoolSize(String initialConnectionsPoolSize) {
        this.initialConnectionsPoolSize = initialConnectionsPoolSize;
    }

    public String getInitialConnectionsPoolSize() {
        return initialConnectionsPoolSize;
    }

    public String getConnectionsPoolSizeRefillThreshold() {
        return connectionsPoolSizeRefillThreshold;
    }

    public void setConnectionsPoolSizeRefillThreshold(String connectionsPoolSizeRefillThreshold) {
        this.connectionsPoolSizeRefillThreshold = connectionsPoolSizeRefillThreshold;
    }

    public String getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(String maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public String getAllowConnectionsPoolExpansion() {
        return allowConnectionsPoolExpansion;
    }

    public void setAllowConnectionsPoolExpansion(String allowConnectionsPoolExpansion) {
        this.allowConnectionsPoolExpansion = allowConnectionsPoolExpansion;
    }

    public String getRootPackageToScan() {
        return rootPackageToScan;
    }

    public void setRootPackageToScan(String rootPackageToScan) {
        this.rootPackageToScan = rootPackageToScan;
    }

    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers() throws SQLException {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
