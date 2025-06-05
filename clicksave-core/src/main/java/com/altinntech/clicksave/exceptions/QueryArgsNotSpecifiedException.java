package com.altinntech.clicksave.exceptions;

public class QueryArgsNotSpecifiedException extends ClicksaveRuntimeException {
    public QueryArgsNotSpecifiedException() {
        super("Query arguments not specified!");
    }
}
