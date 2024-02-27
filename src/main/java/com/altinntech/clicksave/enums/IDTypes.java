package com.altinntech.clicksave.enums;

import java.util.List;
import java.util.UUID;

public enum IDTypes {

    UUID(java.util.UUID.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    ;

    IDTypes(Class<?> type) {
        this.type = type;
    }

    private final Class<?> type;

    public Class<?> getType() {
        return type;
    }

    public static final List<Class<?>> allowedIdTypes = List.of(java.util.UUID.class, Long.class, Integer.class);
}
