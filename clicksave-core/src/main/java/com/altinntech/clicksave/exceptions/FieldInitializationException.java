package com.altinntech.clicksave.exceptions;

/**
 * The {@code FieldInitializationException} class represents an exception thrown when field initialization fails.
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Fyodor Plotnikov
 */
public class FieldInitializationException extends ClicksaveRuntimeException {

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
