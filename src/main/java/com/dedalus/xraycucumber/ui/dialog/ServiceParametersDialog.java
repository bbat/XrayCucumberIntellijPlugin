package com.dedalus.xraycucumber.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;
import javax.swing.*;

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class ServiceParametersDialog extends DialogWrapper {

    private JPanel rootPanel;
    private JTextField urlField;
    private JTextField testProjectField;

    public ServiceParametersDialog(@Nullable Project project) {
        super(project);
        init();

        setTitle("Please provide Jira Settings:");

        urlField.setText("https://jira.qa.dedalus.com/");
        testProjectField.setText("TESTORBIS");
    }

    public JiraServiceParameters createServiceParameters() throws MalformedURLException {
        return new JiraServiceParameters.Builder().url(new URL(urlField.getText())).projectKey(testProjectField.getText()).build();
    }

    @Nullable @Override protected JComponent createCenterPanel() {
        return rootPanel;
    }
}
