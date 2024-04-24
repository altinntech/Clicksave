package com.altinntech.clicksave.core.dto;

/**
 * The {@code QueryMetadata} interface represents metadata for queries.
 * It defines a method to retrieve the query body.
 *
 * <p>This interface is intended to be implemented by classes that provide query metadata.</p>
 *
 * @author Fyodor Plotnikov
 */
public interface QueryMetadata {

    /**
     * Retrieves the query body.
     *
     * @return the query body
     */
    String getQueryBody();
}
