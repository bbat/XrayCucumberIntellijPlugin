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
    private final JPanel mainPanel;

    public XrayCucumberPluginSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Jira URL: "), jiraUrlField, 1, false)
                .addLabeledComponent(new JBLabel("Xray test project name: "), xrayTestProjectField, 1, false)
                .addComponentFillVertically(new JPanel(), 0).getPanel();

        jiraUrlField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Mettez à jour les boutons "Apply" et "OK" ici
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Mettez à jour les boutons "Apply" et "OK" ici
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Mettez à jour les boutons "Apply" et "OK" ici
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
}
