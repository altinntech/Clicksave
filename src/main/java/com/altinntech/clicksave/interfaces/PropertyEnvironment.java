package com.altinntech.clicksave.interfaces;

public interface PropertyEnvironment {

    String getProperty(String propertyName, String defaultValue);
    String getProperty(String propertyName);
}
