package com.altinntech.clicksave.exceptions;

/**
 * The {@code EntityInitializationException} class represents an exception thrown when entity initialization fails.
 * It is a subclass of {@code RuntimeException}.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class EntityInitializationException extends RuntimeException {

    /**
     * Constructs a new EntityInitializationException with no detail message.
     */
    public EntityInitializationException() {
    }

    /**
     * Constructs a new EntityInitializationException with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityInitializationException(String message) {
        super(message);
    }
}
