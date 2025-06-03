package com.altinntech.clicksave.exceptions;

/**
 * Wrapper for internal {@code ClassCacheNotFoundException}
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Anton Volkov
 */
public class ClassCacheNotFoundRuntimeException extends ClicksaveRuntimeException {

    public ClassCacheNotFoundRuntimeException(ClassCacheNotFoundException cause) {
        super(cause.getMessage(), cause);
    }
}
