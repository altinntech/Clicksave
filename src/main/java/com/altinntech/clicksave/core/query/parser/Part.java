package com.altinntech.clicksave.core.query.parser;

/**
 * The {@code Part} interface represents a part of a query.
 * It defines methods to retrieve the part name and determine if the part is a service part.
 *
 * @author Fyodor Plotnikov
 */
public interface Part {
    /**
     * Retrieves the name of the part.
     *
     * @return the part name
     */
    String getPartName();

    /**
     * Determines whether the part is a service part.
     *
     * @return {@code true} if the part is a service part, {@code false} otherwise
     */
    boolean isServicePart();
}
