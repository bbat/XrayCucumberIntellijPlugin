package com.dedalus.xraycucumber.ui;

import com.dedalus.xraycucumber.service.ProgressReporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

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

    public void showIssueDetailsInPopup(JsonArray issues, Project project) {
        String details = formatIssueDetails(issues);

        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showMessageDialog(
                    project,
                    details,
                    "Following cucumber test cases have been created/updated in Xray",
                    Messages.getInformationIcon()
            );
        });

    }

    private String formatIssueDetails(JsonArray issues) {
        if (issues != null) {
            StringBuilder details = new StringBuilder();

            for (JsonElement element : issues) {
                JsonObject issue = element.getAsJsonObject();
                details.append("Scenario: ").append(issue.get("summary").getAsString()).append("\n");
                details.append("Xray IssueKey: ").append(issue.get("key").getAsString())
                        .append(", Test case type : ").append(issue.get("issueType").getAsJsonObject().get("name")).append("\n\n");
            }

            return details.toString();
        } else {
            throw new IllegalStateException("Issues is null");
        }
    }
}
