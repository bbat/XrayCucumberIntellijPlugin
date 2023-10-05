package com.dedalus.xraycucumber.ui.dialog;

import javax.annotation.Nullable;
import javax.swing.*;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.ui.utils.CredentialManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class JiraCredentialsDialog extends DialogWrapper {

    private final ServiceParameters serviceParameters;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox storeCredentialsCheckBox;
    private JPanel rootPanel;

    public JiraCredentialsDialog(@Nullable Project project, ServiceParameters serviceParameters) {
        super(project);
        this.serviceParameters = serviceParameters;
        init();
        setTitle("Credential for " + serviceParameters.getUrl().toExternalForm());
        usernameField.setText(serviceParameters.getUsername());
        passwordField.setText(serviceParameters.getPassword());

        CredentialManager credentialManager = new CredentialManager();
        storeCredentialsCheckBox.setSelected(credentialManager.storeByDefault());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public ServiceParameters getUpdatedServiceParameters() {
        return new ServiceParameters.Builder()
                .url(serviceParameters.getUrl())
                .username(usernameField.getText())
                .password(String.copyValueOf(passwordField.getPassword()))
                .projectKey(serviceParameters.getProjectKey())
                .build();
    }

    public boolean storeCredentials() {
        return storeCredentialsCheckBox.isSelected();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return usernameField.getText().isEmpty() ? usernameField : passwordField;
    }
}
