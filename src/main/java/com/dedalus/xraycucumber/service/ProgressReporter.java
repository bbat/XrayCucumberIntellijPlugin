package com.dedalus.xraycucumber.service;
public interface ProgressReporter {
    void reportProgress(String message, double completionRatio);

    void reportSuccess(String message);

    void reportError(String message, Exception exception);

}
