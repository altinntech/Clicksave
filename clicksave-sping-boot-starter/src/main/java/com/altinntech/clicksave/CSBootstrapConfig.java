package com.altinntech.clicksave;

import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@Configuration
@AutoConfiguration
public class CSBootstrapConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    @ConditionalOnMissingBean(CSBootstrap.class)
    public CSBootstrap csBootstrap() throws ClassCacheNotFoundException, SQLException {
        DefaultProperties defaultProperties = DefaultProperties.fromEnvironment(environment);
        return new CSBootstrap(defaultProperties, meterRegistry);
    }
}
