package com.altinntech.clicksave.core;

import cc.blynk.clickhouse.ClickHouseDataSource;
import cc.blynk.clickhouse.settings.ClickHouseQueryParam;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.enums.Metrics;
import com.altinntech.clicksave.interfaces.ClicksaveMetrics;
import com.altinntech.clicksave.interfaces.Observer;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.altinntech.clicksave.core.CSUtils.convertJdbcUrlToHttpUrl;
import static com.altinntech.clicksave.log.CSLogger.*;

/**
 * The {@code ConnectionManager} class manages connections to the database and serves as a connection pool.
 * It handles the creation, retrieval, and release of database connections.
 *
 * @author Fyodor Plotnikov
 */
public class ConnectionManager implements Observer {

    private final DefaultProperties defaultProperties;

    private String URL;
    private String HTTP_URL;
    private String USER;
    private String PASSWORD;
    private final int INITIAL_POOL_SIZE;
    private final int REFILL_POOL_SIZE_THRESHOLD;
    @Getter
    private final int MAX_POOL_SIZE;
    private final boolean ALLOW_EXPANSION;
    private final int CONNECTION_RETRY_TIME = 2000;

    private final Deque<Connection> connectionPool = new ConcurrentLinkedDeque<>();
    // TODO Configurable driver class since Clickhouse4j is not longer supported and causes dependency problems
    private DataSource dataSource;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int extendedPoolSize = 0;

    private boolean debounce = false;

    // Metrics
    private final ClicksaveMetrics metrics;

    public ConnectionManager(DefaultProperties defaultProperties) throws SQLException {
        this(defaultProperties, ClicksaveMetrics.noop());
    }

    public ConnectionManager(DefaultProperties defaultProperties, ClicksaveMetrics metrics) throws SQLException {
        this.defaultProperties = defaultProperties;
        this.metrics = metrics;
        defaultProperties.registerObserver(this);

        //--Initialize Variables --//
        this.INITIAL_POOL_SIZE = Integer.parseInt(defaultProperties.getInitialConnectionsPoolSize());
        this.REFILL_POOL_SIZE_THRESHOLD = Integer.parseInt(defaultProperties.getConnectionsPoolSizeRefillThreshold());
        this.MAX_POOL_SIZE = Integer.parseInt(defaultProperties.getMaxConnectionPoolSize());
        this.ALLOW_EXPANSION = Boolean.parseBoolean(defaultProperties.getAllowConnectionsPoolExpansion());

        metrics.registerNumValueCheck(Metrics.CONNECTIONS_COUNT.getMetricsName(), connectionPool::size);
        metrics.registerNumValueCheck(Metrics.CONNECTIONS_MAX_POOL_SIZE.getMetricsName(), this::getMAX_POOL_SIZE);
        metrics.registerNumValueCheck(Metrics.CONNECTIONS_POOL_SIZE.getMetricsName(), () -> INITIAL_POOL_SIZE + extendedPoolSize);

        if (defaultProperties.validate()) {
            this.URL = defaultProperties.getUrl();
            this.HTTP_URL = convertJdbcUrlToHttpUrl(URL);
            this.USER = defaultProperties.getUsername();
            this.PASSWORD = defaultProperties.getPassword();

            createDataSource(true);
        }
    }

    /**
     * Constructs a new ConnectionManager instance.
     */
    public ConnectionManager(ClicksaveMetrics metrics) throws SQLException {
        this(DefaultProperties.fromPropertyFile(), metrics);
    }

    /**
     * Retrieves a database connection from the connection pool.
     *
     * @return the database connection
     * @throws SQLException if a SQL exception occurs
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            createConnection();
            expandPool();
        }
        if (connectionPool.size() < REFILL_POOL_SIZE_THRESHOLD) {
            refillPool();
        }

        Connection connection = connectionPool.pop();
        return connection;
    }

    private synchronized void expandPool() {
        if (ALLOW_EXPANSION && extendedPoolSize + INITIAL_POOL_SIZE+2 < MAX_POOL_SIZE) {
            extendedPoolSize +=2;
        }
    }

    /**
     * Releases a database connection.
     *
     * @param connection the database connection to release
     * @throws SQLException if a SQL exception occurs
     */
    public synchronized void releaseConnection(Connection connection) throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    private synchronized void refillPool() {
        CompletableFuture.runAsync(() -> {
            while (connectionPool.size() < INITIAL_POOL_SIZE + extendedPoolSize) {
                try {
                    createConnection();
                } catch (SQLException e) {
                    error(e.getMessage(), this.getClass());
                }
            }
        }, executorService);
    }

    private synchronized void createConnection() throws SQLException {
        Connection connection;
        connection = dataSource.getConnection();
        connectionPool.push(connection);
    }

    /**
     * Closes all database connections.
     */
    public synchronized void closeAllConnections() throws SQLException {
        if (debounce)
            return;
        debounce = true;
        for (Connection connection : connectionPool) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        connectionPool.clear();
        info("All connections to the database are closed");
        debounce = false;
    }

    @Override
    public void update() throws SQLException {
        this.URL = defaultProperties.getUrl();
        this.USER = defaultProperties.getUsername();
        this.PASSWORD = defaultProperties.getPassword();

        createDataSource(false);
    }

    private void createDataSource(boolean isFirstInit) throws SQLException {
        if (!isFirstInit)
            closeAllConnections();
        info("Set up data source...");
        Properties properties = new Properties();
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);
        properties.setProperty(ClickHouseQueryParam.COMPRESS.getKey(), defaultProperties.getConnectionCompressionEnabled());
        healthCheck();
        this.dataSource = new ClickHouseDataSource(URL, properties);
        try {
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                createConnection();
            }
            info("Connection to " + URL + " established");
        } catch (SQLException e) {
            error("Error while create connection: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void healthCheck() {
        while (!healthCheck(HTTP_URL)) {
            debug("Health check", "Retry connect to " + HTTP_URL);
            metrics.incrementCounter(Metrics.HEALTH_CHECK_FAILED.getMetricsName());
            Thread.sleep(CONNECTION_RETRY_TIME);
        }
    }

    private boolean healthCheck(String url) {
        try {
            java.net.URL healthCheckUrl = new java.net.URL(url);
            HttpURLConnection connection = (HttpURLConnection) healthCheckUrl.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                debug("Health check", "Ok. Response code from " + HTTP_URL + ": " + responseCode);
                return true;
            } else {
                debug("Health check", "Failed. Response code from " + HTTP_URL + ": " + responseCode);
                return false;
            }
        } catch (IOException e) {
            debug("Health check", "Health check failed" + e.getMessage());
            return false;
        }
    }
}
