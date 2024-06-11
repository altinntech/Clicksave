package com.altinntech.clicksave.metrics.dto.base;

import com.altinntech.clicksave.metrics.dto.ConnectionManagerMetrics;
import com.altinntech.clicksave.metrics.dto.ThreadPoolManagerMetrics;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsLog {

    Map<String, Object> properties;

    @JsonProperty("connectionManager")
    ConnectionManagerMetrics connectionManagerMetrics;

    @JsonProperty("threadPoolManager")
    ThreadPoolManagerMetrics threadPoolManagerMetrics;
}
