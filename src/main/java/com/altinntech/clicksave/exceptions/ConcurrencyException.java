package com.altinntech.clicksave.exceptions;

/**
 * Wrapper for InterruptedException
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Anton Volkov
 */
public class ConcurrencyException extends ClicksaveRuntimeException {

    public ConcurrencyException(InterruptedException cause) {
        super("Concurrency Exception: " + cause.getMessage(), cause);
    }
}
