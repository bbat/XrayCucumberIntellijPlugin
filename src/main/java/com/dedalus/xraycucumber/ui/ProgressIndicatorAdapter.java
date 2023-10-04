package com.dedalus.xraycucumber.ui;

import com.dedalus.xraycucumber.service.ProgressReporter;
import com.dedalus.xraycucumber.ui.utils.NotificationUtils;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class ProgressIndicatorAdapter implements ProgressReporter {

    private final ProgressIndicator progressIndicator;
    private final Project project;

    public ProgressIndicatorAdapter(ProgressIndicator progressIndicator, Project project) {
        this.progressIndicator = progressIndicator;
        this.project = project;
    }

    @Override
    public void reportProgress(String message, double completionRatio) {
        progressIndicator.setText(message);
        progressIndicator.setFraction(completionRatio);
    }

    @Override
    public void reportSuccess(String message) {
        NotificationUtils.notifySuccess(project, message);
    }

    @Override
    public void reportError(String message, Exception exception) {
        NotificationUtils.notifyError(project, message);
    }
}
