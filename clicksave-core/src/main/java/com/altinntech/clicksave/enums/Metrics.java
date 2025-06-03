package com.altinntech.clicksave.enums;

public enum Metrics {
    REPO_SAVE_COUNTER("repo_save_counter"),
    REPO_UPDATE_COUNTER("repo_update_counter"),
    REPO_FETCH_COUNTER("repo_fetch_counter"),
    REPO_DELETE_COUNTER("repo_delete_counter"),
    BATCH_SUCCESS("batch_success"),
    BATCH_FAIL("batch_fail"),
    HEALTH_CHECK_FAILED("health_check_failed"),
    CONNECTIONS_COUNT("connections_count"),
    CONNECTIONS_POOL_SIZE("connections_pool_size"),
    CONNECTIONS_MAX_POOL_SIZE("connections_max_pool_size")
    ;

    private final String metricsName;

    Metrics(String metricsName) {
        this.metricsName = metricsName;
    }

    public String getMetricsName() {
        return metricsName;
    }
}
