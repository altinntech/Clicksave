package com.altinntech.clicksave.core;

import org.springframework.core.env.Environment;

/**
 * The {@code DefaultProperties} class provides default properties for configuring the application.
 * It retrieves properties from the environment and provides default values if not specified.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class DefaultProperties {

    private final Environment environment;

    private final String url = "jdbc:clickhouse://localhost:8123/default";
    private final String username = "";
    private final String password = "";
    private final String initialConnectionsPoolSize = "20";
    private final String connectionsPoolSizeRefillThreshold = "5";
    private final String maxConnectionPoolSize = "50";
    private final String allowConnectionsPoolExpansion = "true";
    private final String rootPackageToScan = "";

    /**
     * Constructs a new DefaultProperties instance.
     *
     * @param environment the environment
     */
    public DefaultProperties(Environment environment) {
        this.environment = environment;
    }

    /**
     * Gets the URL for the data source.
     *
     * @return the URL
     */
    public String getUrl() {
        return environment.getProperty("clicksave.connection.datasource.url", url);
    }

    /**
     * Gets the username for the data source.
     *
     * @return the username
     */
    public String getUsername() {
        return environment.getProperty("clicksave.connection.datasource.username", username);
    }

    /**
     * Gets the password for the data source.
     *
     * @return the password
     */
    public String getPassword() {
        return environment.getProperty("clicksave.connection.datasource.password", password);
    }

    /**
     * Gets the initial size of the connections pool.
     *
     * @return the initial size
     */
    public Integer getInitialConnectionsPoolSize() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.initial-size", initialConnectionsPoolSize));
    }

    /**
     * Gets the refill threshold for the connections pool size.
     *
     * @return the refill threshold
     */
    public Integer getConnectionsPoolSizeRefillThreshold() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.refill-threshold", connectionsPoolSizeRefillThreshold));
    }

    /**
     * Gets the maximum size of the connections pool.
     *
     * @return the maximum size
     */
    public Integer getMaxConnectionPoolSize() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.max-size", maxConnectionPoolSize));
    }

    /**
     * Determines whether connections pool expansion is allowed.
     *
     * @return true if expansion is allowed, false otherwise
     */
    public Boolean getAllowConnectionsPoolExpansion() {
        return Boolean.parseBoolean(environment.getProperty("clicksave.connection.pool.allow-expansion", allowConnectionsPoolExpansion));
    }

    /**
     * Gets the root package to scan.
     *
     * @return the root package
     */
    public String getRootPackageToScan() {
        return environment.getProperty("clicksave.core.root-package", rootPackageToScan);
    }
}
