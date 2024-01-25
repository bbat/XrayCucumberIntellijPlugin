package com.dedalus.xraycucumber.ui;

import java.awt.*;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class SynchroStartPopup extends DialogWrapper {
    private volatile boolean canceled = false;
    String title = "Xray feature synchronization";

    public SynchroStartPopup(final Project project) {
        super(project);
        setTitle(title);
        init();
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea("""
                This feature will be uploaded to Jira Xray:
                Each scenario in this feature will be converted into a Xray Test Case
                Xray Test Case Id will be added to the corresponding scenario as a tag
                """);

        textArea.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(textArea, BorderLayout.CENTER);

        return dialogPanel;
    }

    @Override public void doCancelAction() {
        canceled = true;
        super.doCancelAction();
    }
}
