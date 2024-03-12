package com.altinntech.clicksave.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import static com.altinntech.clicksave.log.CSLogger.error;

public class PropertyReader {

    private static PropertyReader instance;

    private final Properties properties = new Properties();

    private PropertyReader() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            error("Error loading properties" + e.getMessage());
        }
    }

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        if (containsKey(key)) {
            return getProperty(key);
        } else {
            return defaultValue;
        }
    }

    public Set<String> getAllPropertyNames() {
        return properties.stringPropertyNames();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
}
