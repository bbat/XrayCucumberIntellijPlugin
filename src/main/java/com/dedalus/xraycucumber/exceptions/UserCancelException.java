package com.dedalus.xraycucumber.exceptions;

public class UserCancelException extends RuntimeException {
    public UserCancelException() {
        super("User cancelled the operation");
    }
}
