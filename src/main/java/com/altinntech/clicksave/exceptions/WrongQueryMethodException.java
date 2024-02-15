package com.altinntech.clicksave.exceptions;

public class WrongQueryMethodException extends RuntimeException {

    public WrongQueryMethodException() {
    }

    public WrongQueryMethodException(String message) {
        super(message);
    }
}
