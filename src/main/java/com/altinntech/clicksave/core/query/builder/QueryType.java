package com.altinntech.clicksave.core.query.builder;

/**
 * The {@code QueryType} enum is used to specify the type of query.
 * It defines different types such as SELECT, INSERT, UPDATE, DELETE, or ANY.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public enum QueryType {

    /**
     * Indicates a SELECT query type.
     */
    SELECT,

    /**
     * Indicates an INSERT query type.
     */
    INSERT,

    /**
     * Indicates an UPDATE query type.
     */
    UPDATE,

    /**
     * Indicates a DELETE query type.
     */
    DELETE,

    /**
     * Indicates any query type.
     */
    ANY,
}

