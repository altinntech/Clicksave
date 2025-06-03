package com.altinntech.clicksave.core.query.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public static QueryPullType getByJavaType(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                if (rawClass.equals(Optional.class)) {
                    return QueryPullType.SINGLE;
                } else if (rawClass.equals(List.class)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?>) {
                        return QueryPullType.MULTIPLE;
                    }
                }
            }
        } else if (type instanceof Class<?>) {
            return QueryPullType.NONE;
        }
        return null;
    }
}
