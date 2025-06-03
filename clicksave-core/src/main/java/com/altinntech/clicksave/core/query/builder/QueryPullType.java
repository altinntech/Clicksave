package com.altinntech.clicksave.core.query.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The {@code QueryPullType} enum is used to define the entity retrieval mode.
 * It specifies whether the query retrieves a single entity, multiple entities, or none.
 *
 * @author Fyodor Plotnikov
 */
public enum QueryPullType {

    /**
     * Indicates a single entity retrieval mode.
     */
    SINGLE,

    /**
     * Indicates a multiple entities retrieval mode.
     */
    MULTIPLE,

    /**
     * Indicates a auto entities retrieval mode.
     */
    AUTO,

    /**
     * Indicates no entity retrieval mode.
     */
    NONE,
    ;

    public static QueryPullType getByReturnType(Class<?> type) {
        if (type == null) {
            return NONE;
        }
        if (type.isAssignableFrom(List.class)) {
            return MULTIPLE;
        } else if (type.isAssignableFrom(Optional.class)) {
            return SINGLE;
        }
        return NONE;
    }
}
