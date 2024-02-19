package com.altinntech.clicksave.core;

import org.springframework.core.env.Environment;

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

    public DefaultProperties(Environment environment) {
        this.environment = environment;
    }

    public String getUrl() {
        return environment.getProperty("clicksave.connection.datasource.url", url);
    }

    public String getUsername() {
        return environment.getProperty("clicksave.connection.datasource.username", username);
    }

    public String getPassword() {
        return environment.getProperty("clicksave.connection.datasource.password", password);
    }

    public Integer getInitialConnectionsPoolSize() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.initial-size", initialConnectionsPoolSize));
    }

    public Integer getConnectionsPoolSizeRefillThreshold() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.refill-threshold", connectionsPoolSizeRefillThreshold));
    }

    public Integer getMaxConnectionPoolSize() {
        return Integer.parseInt(environment.getProperty("clicksave.connection.pool.max-size", maxConnectionPoolSize));
    }

    public Boolean getAllowConnectionsPoolExpansion() {
        return Boolean.parseBoolean(environment.getProperty("clicksave.connection.pool.allow-expansion", allowConnectionsPoolExpansion));
    }

    public String getRootPackageToScan() {
        return environment.getProperty("clicksave.core.root-package", rootPackageToScan);
    }
}
