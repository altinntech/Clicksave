package com.altinntech.clicksave.exceptions;

/**
 * The {@code FieldInitializationException} class represents an exception thrown when field initialization fails.
 * It is a subclass of {@code RuntimeException}.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
public class FieldInitializationException extends RuntimeException {

    /**
     * Constructs a new FieldInitializationException with no detail message.
     */
    public FieldInitializationException() {
    }

    /**
     * Constructs a new FieldInitializationException with the specified detail message.
     *
     * @param message the detail message
     */
    public FieldInitializationException(String message) {
        super(message);
    }
}
