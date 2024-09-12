package com.altinntech.clicksave.exceptions;

/**
 * Superclass for unchecked Exceptions
 *
 * @author Anton Volkov
 */
public abstract class ClicksaveRuntimeException extends RuntimeException {

    protected ClicksaveRuntimeException() {}

    protected ClicksaveRuntimeException(String message) {
        super(message);
    }

    protected ClicksaveRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
