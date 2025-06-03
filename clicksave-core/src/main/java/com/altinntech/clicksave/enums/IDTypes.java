package com.altinntech.clicksave.enums;

import java.util.List;
import java.util.UUID;

public enum IDTypes {

    UUID(UUID.class),
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

    public static final List<Class<?>> allowedIdTypes = List.of(UUID.class, Long.class, Integer.class);
}
