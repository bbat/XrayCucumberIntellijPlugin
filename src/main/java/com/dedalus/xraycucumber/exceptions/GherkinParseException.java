package com.dedalus.xraycucumber.exceptions;

public class GherkinParseException extends RuntimeException {

    public GherkinParseException(String message) {
        super(message);
    }

    public GherkinParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
