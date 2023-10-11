package com.dedalus.xraycucumber.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class SynchroStartPopup {

    private final Project project;

    public SynchroStartPopup(final Project project) {
        this.project = project;
    }

    public boolean show() {
        String message = "This feature will be uploaded to Jira Xray: \n" +
                "Each scenario in this feature will be converted into a Xray Test Case\n" +
                "Xray Test Case Id will be added to the corresponding scenario as a tag\n" +
                "Please Note that this can take up to ten seconds, depending on the size of your feature and Jira's state of readiness.\n";

        String title = "You are about to synchronize this feature file with Xray";

        int result = Messages.showOkCancelDialog(project, message, title, Messages.getQuestionIcon());

        return result == Messages.OK;
    }
}
