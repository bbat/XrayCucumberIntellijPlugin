package com.dedalus.xraycucumber.ui.dialog;

import javax.annotation.Nullable;
import javax.swing.*;

import com.dedalus.xraycucumber.serviceparameters.CredentialManager;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.PasswordSafeWrapper;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;

public class JiraCredentialsDialog extends DialogWrapper {

    private JBTextField usernameField;
    private JBPasswordField passwordField;
    private JCheckBox storeCredentialsCheckBox;
    private JPanel rootPanel;

    public JiraCredentialsDialog(@Nullable Project project, JiraServiceParameters serviceParameters) {
        super(project);
        init();
        setTitle("Credential for " + serviceParameters.getUrl().toExternalForm());
        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        storeCredentialsCheckBox.setSelected(credentialManager.storeByDefault());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public Credentials getCredentialsFromUser() {
        return new Credentials(
                usernameField.getText(),
                String.valueOf(passwordField.getPassword())
        );
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
