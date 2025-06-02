package com.altinntech.clicksave.exceptions;

public class IdGeneratorNotFoundException extends ClicksaveRuntimeException {

    public IdGeneratorNotFoundException(Class<?> idClass) {
        this(idClass.getSimpleName());
    }

    public IdGeneratorNotFoundException(String type) {
        super(String.format("Id generator for class %s not found!", type));
    }
}
