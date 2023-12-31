package com.dedalus.xraycucumber.ui.dialog;

import javax.annotation.Nullable;
import javax.swing.*;

import com.dedalus.xraycucumber.serviceparameters.CredentialManager;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.PasswordSafeWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;

public class JiraCredentialsDialog extends DialogWrapper {

    private final JiraServiceParameters serviceParameters;
    private JBTextField usernameField;
    private JBPasswordField passwordField;
    private JCheckBox storeCredentialsCheckBox;
    private JPanel rootPanel;

    public JiraCredentialsDialog(@Nullable Project project, JiraServiceParameters serviceParameters) {
        super(project);
        this.serviceParameters = serviceParameters;
        init();
        setTitle("Credential for " + serviceParameters.getUrl().toExternalForm());
        usernameField.setText(serviceParameters.getUsername());
        passwordField.setText(serviceParameters.getPassword());

        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        storeCredentialsCheckBox.setSelected(credentialManager.storeByDefault());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public JiraServiceParameters getUpdatedServiceParameters() {
        return new JiraServiceParameters.Builder()
                .url(serviceParameters.getUrl())
                .username(usernameField.getText())
                .password(String.copyValueOf(passwordField.getPassword()))
                .projectKey(serviceParameters.getProjectKey())
                .bearerToken(serviceParameters.getBearerToken())
                .tokenAuthenticationEnabled(serviceParameters.isTokenAuthenticationEnabled())
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
