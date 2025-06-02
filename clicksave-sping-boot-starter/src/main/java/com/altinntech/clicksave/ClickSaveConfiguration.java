package com.altinntech.clicksave;

import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.core.utils.MicrometerMetrics;
import com.altinntech.clicksave.core.utils.SpringEnvironment;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.ClicksaveMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.thepavel.icomponent.InterfaceComponentScan;

import java.sql.SQLException;

@ComponentScan
@Configuration
@AutoConfiguration
@InterfaceComponentScan
public class ClickSaveConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    @ConditionalOnMissingBean(CSBootstrap.class)
    public CSBootstrap csBootstrap(ClicksaveMetrics metrics) throws ClassCacheNotFoundException, SQLException {
        DefaultProperties defaultProperties = DefaultProperties.getProperties(new SpringEnvironment(environment));
        return new CSBootstrap(defaultProperties, metrics);
    }

    @Bean
    @ConditionalOnMissingBean(ClicksaveMetrics.class)
    public ClicksaveMetrics noOpClickSaveMetricsApi() {
        return ClicksaveMetrics.noop();
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    public ClicksaveMetrics micrometerMetrics(MeterRegistry meterRegistry) {
        return new MicrometerMetrics(meterRegistry);
    }
}
