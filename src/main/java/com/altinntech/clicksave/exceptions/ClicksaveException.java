package com.altinntech.clicksave.exceptions;

/**
 * Superclass for checked Exceptions
 *
 * @author Anton Volkov
 */
public abstract class ClicksaveException extends Exception {

    protected ClicksaveException() {}

    protected ClicksaveException(String message) {
        super(message);
    }

    protected ClicksaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
