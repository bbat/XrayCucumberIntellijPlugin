package com.dedalus.xraycucumber.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class IssueDetailsDisplayer {

    private final Project project;

    public IssueDetailsDisplayer(Project project) {
        this.project = project;
    }

    public void showInPopup(JsonArray issues) {
        String details = formatIssueDetails(issues);
        ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(
                project,
                details,
                "Following Cucumber Test Cases Have Been Created/Updated in Xray",
                Messages.getInformationIcon()
        ));
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
