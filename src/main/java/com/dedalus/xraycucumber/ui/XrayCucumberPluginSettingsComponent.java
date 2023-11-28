package com.dedalus.xraycucumber.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

public class XrayCucumberPluginSettingsComponent {

    private final JBTextField xrayTestProjectField = new JBTextField();
    private final JBTextField jiraUrlField = new JBTextField();
    private final JBTextField bearerTokenField = new JBTextField();
    private final JCheckBox tokenAuthenticationCheckBox = new JCheckBox("Token Authentication");
    private final JCheckBox saveFeatureBeforeUpdCheckBox = new JCheckBox("Save feature file before update");

    private final JPanel mainPanel;

    public XrayCucumberPluginSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Jira URL: "), jiraUrlField, 1, false)
                .addLabeledComponent(new JBLabel("Xray test project name: "), xrayTestProjectField, 1, false)
                .addLabeledComponent(new JBLabel("Bearer token: "), bearerTokenField, 1, false)
                .addComponent(tokenAuthenticationCheckBox)
                .addComponent(saveFeatureBeforeUpdCheckBox)
                .addComponentFillVertically(new JPanel(), 0).getPanel();

        jiraUrlField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    public JComponent getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return jiraUrlField;
    }

    @NotNull
    public String getXrayTestProjectField() {
        return xrayTestProjectField.getText();
    }

    public void setXrayTestProjectField(@NotNull String newXrayTestProjectField) {
        this.xrayTestProjectField.setText(newXrayTestProjectField);
    }

    @NotNull
    public String getJiraUrlField() {
        return jiraUrlField.getText();
    }

    public void setJiraUrlField(@NotNull String newJiraUrlField) {
        this.jiraUrlField.setText(newJiraUrlField);
    }

    @NotNull
    public String getBearerTokenField() {
        return bearerTokenField.getText();
    }

    public void setBearerTokenField(@NotNull String newBearerTokenField) {
        this.bearerTokenField.setText(newBearerTokenField);
    }

    public boolean isTokenAuthenticationCheckBoxSelected() {
        return tokenAuthenticationCheckBox.isSelected();
    }

    public void setTokenAuthenticationCheckBoxSelected(boolean selected) {
        this.tokenAuthenticationCheckBox.setSelected(selected);
    }

    public boolean isSaveFeatureBeforeUpdCheckBoxSelected() {
        return saveFeatureBeforeUpdCheckBox.isSelected();
    }

    public void setSaveFeatureBeforeUpdCheckBoxSelected(boolean selected) {
        this.saveFeatureBeforeUpdCheckBox.setSelected(selected);
    }
}
