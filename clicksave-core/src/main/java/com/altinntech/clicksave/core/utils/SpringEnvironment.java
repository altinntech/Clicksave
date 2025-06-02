package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.interfaces.PropertyEnvironment;
import lombok.Data;
import org.springframework.core.env.Environment;

public record SpringEnvironment(Environment environment) implements PropertyEnvironment {

    @Override
    public String getProperty(String propertyName, String defaultValue) {
        return environment.getProperty(propertyName, defaultValue);
    }

    @Override
    public String getProperty(String propertyName) {
        return environment.getProperty(propertyName);
    }
}
