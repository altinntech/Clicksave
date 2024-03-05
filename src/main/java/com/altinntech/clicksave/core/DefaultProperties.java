package com.altinntech.clicksave.core;

import com.altinntech.clicksave.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code DefaultProperties} class provides default properties for configuring the application.
 * It retrieves properties from the environment and provides default values if not specified.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */

public class DefaultProperties {

    private List<Observer> observers = new ArrayList<>();

    private String url = "jdbc:clickhouse://localhost:8123/default";
    private String username = "";
    private String password = "";
    private String initialConnectionsPoolSize = "20";
    private String connectionsPoolSizeRefillThreshold = "5";
    private String maxConnectionPoolSize = "50";
    private String allowConnectionsPoolExpansion = "true";
    private String rootPackageToScan = "";

    public static DefaultProperties fromEnvironment() {
        DefaultProperties defaultProperties = new DefaultProperties();
        PropertyReader propertyReader = PropertyReader.getInstance();
        defaultProperties.url = propertyReader.getProperty("clicksave.connection.datasource.url");
        defaultProperties.username = propertyReader.getProperty("clicksave.connection.datasource.username");
        defaultProperties.password = propertyReader.getProperty("clicksave.connection.datasource.password");
        defaultProperties.initialConnectionsPoolSize = propertyReader.getProperty("clicksave.connection.pool.initial-size");
        defaultProperties.connectionsPoolSizeRefillThreshold = propertyReader.getProperty("clicksave.connection.pool.refill-threshold");
        defaultProperties.maxConnectionPoolSize = propertyReader.getProperty("clicksave.connection.pool.max-size");
        defaultProperties.allowConnectionsPoolExpansion = propertyReader.getProperty("clicksave.connection.pool.allow-expansion");
        defaultProperties.rootPackageToScan = propertyReader.getProperty("clicksave.core.root-package");
        return defaultProperties;
    }

    public void reloadProperties() {
        PropertyReader propertyReader = PropertyReader.getInstance();
        url = propertyReader.getProperty("clicksave.connection.datasource.url", url);
        username = propertyReader.getProperty("clicksave.connection.datasource.username", username);
        password = propertyReader.getProperty("clicksave.connection.datasource.password", password);
        initialConnectionsPoolSize = propertyReader.getProperty("clicksave.connection.pool.initial-size", initialConnectionsPoolSize);
        connectionsPoolSizeRefillThreshold = propertyReader.getProperty("clicksave.connection.pool.refill-threshold", connectionsPoolSizeRefillThreshold);
        maxConnectionPoolSize = propertyReader.getProperty("clicksave.connection.pool.max-size", maxConnectionPoolSize);
        allowConnectionsPoolExpansion = propertyReader.getProperty("clicksave.connection.pool.allow-expansion", allowConnectionsPoolExpansion);
        rootPackageToScan = propertyReader.getProperty("clicksave.core.root-package", rootPackageToScan);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        notifyObservers();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyObservers();
    }

    public void setPassword(String password) {
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

    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
