package com.altinntech.clicksave.exceptions;

/**
 * Wrapper for reflective operation exceptions
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Anton Volkov
 */
public class ReflectiveException extends ClicksaveRuntimeException {

    public ReflectiveException(ReflectiveOperationException cause) {
        super("Reflective Exception: " + cause.getMessage(), cause);
    }
}
