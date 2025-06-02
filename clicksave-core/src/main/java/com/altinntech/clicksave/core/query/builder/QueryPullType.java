package com.altinntech.clicksave.core.query.builder;

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
}
