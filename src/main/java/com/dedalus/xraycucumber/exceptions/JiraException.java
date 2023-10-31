package com.dedalus.xraycucumber.exceptions;

// Custom exception class
public class JiraException extends RuntimeException {

    public JiraException(String message) {
        super(message);
    }
}
