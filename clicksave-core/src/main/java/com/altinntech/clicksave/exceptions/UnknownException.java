package com.altinntech.clicksave.exceptions;

/**
 * Wrapper for unknown exceptions
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Anton Volkov
 */
public class UnknownException extends ClicksaveRuntimeException {

    public UnknownException(Exception cause) {
            super("Unknown Exception: " + cause.getMessage(), cause);
    }
}