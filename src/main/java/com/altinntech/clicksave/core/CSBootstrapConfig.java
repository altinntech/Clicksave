package com.altinntech.clicksave.core;

import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfiguration
public class CSBootstrapConfig {

    @Bean
    @ConditionalOnMissingBean(CSBootstrap.class)
    public CSBootstrap csBootstrap() throws ClassCacheNotFoundException {
        DefaultProperties defaultProperties = DefaultProperties.fromEnvironment();
        return new CSBootstrap(defaultProperties);
    }
}
