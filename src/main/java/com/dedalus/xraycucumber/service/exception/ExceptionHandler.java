package com.dedalus.xraycucumber.service.exception;

import javax.annotation.Nullable;

import com.dedalus.xraycucumber.service.ProgressReporter;

public class ExceptionHandler {
    public void handle(Exception e, @Nullable ProgressReporter reporter, String message) {
        if (reporter != null) {
            reporter.reportError(message, e);
        } else {
            throw new RuntimeException(message, e);
        }
    }

}
