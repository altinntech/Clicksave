package com.altinntech.clicksave.core.query.builder;

/**
 * The {@code QueryPullType} enum is used to define the entity retrieval mode.
 * It specifies whether the query retrieves a single entity, multiple entities, or none.
 *
 * <p>Author: Fyodor Plotnikov</p>
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
     * Indicates no entity retrieval mode.
     */
    NONE,
}
