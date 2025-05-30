package com.altinntech.clicksave.exceptions;


/**
 * Wrapper for SQL exceptions
 * It is a subclass of {@code ClicksaveRuntimeException}.
 *
 * @author Anton Volkov
 */
public class ClicksaveSQLException extends ClicksaveRuntimeException {

    public ClicksaveSQLException(java.sql.SQLException cause) {
            super("SQL Exception: " + cause.getMessage(), cause);
        }
}