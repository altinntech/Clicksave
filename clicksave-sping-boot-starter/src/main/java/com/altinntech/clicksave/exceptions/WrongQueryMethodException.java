package com.altinntech.clicksave.exceptions;

/**
 * The {@code WrongQueryMethodException} class represents an exception thrown when an incorrect query method is encountered.
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Fyodor Plotnikov
 */
public class WrongQueryMethodException extends ClicksaveRuntimeException {

    /**
     * Constructs a new WrongQueryMethodException with no detail message.
     */
    public WrongQueryMethodException() {
    }

    /**
     * Constructs a new WrongQueryMethodException with the specified detail message.
     *
     * @param message the detail message
     */
    public WrongQueryMethodException(String message) {
        super(message);
    }
}

