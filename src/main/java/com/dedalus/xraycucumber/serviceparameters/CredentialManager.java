package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;
import java.util.Optional;

import com.dedalus.xraycucumber.exceptions.UserCancelException;
import com.dedalus.xraycucumber.ui.dialog.JiraCredentialsDialog;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.project.Project;

public class CredentialManager {

    private final PasswordSafeWrapper passwordSafeWrapper;

    public CredentialManager(PasswordSafeWrapper passwordSafeWrapper) {
        this.passwordSafeWrapper = passwordSafeWrapper;
    }

    public JiraServiceParameters retrieveCredentialsFromStoreIfUndefined(JiraServiceParameters serviceParameters) {
        JiraServiceParameters updatedParameters = Optional.ofNullable(passwordSafeWrapper.get(createCredentialAttributes(serviceParameters.getUrl())))
                .map(c -> {
                    if (serviceParameters.getUsername() == null) {
                        serviceParameters.setUsername(c.getUserName());
                    }
                    if (serviceParameters.getPassword() == null) {
                        serviceParameters.setPassword(c.getPasswordAsString());
                    }
                    return serviceParameters;
                })
                .orElse(serviceParameters);

        return updatedParameters;
    }

    public JiraServiceParameters requestJiraCredentialsFromUser(Project project, JiraServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = createJiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            serviceParameters.setUsername(jiraCredentialsDialog.getUpdatedServiceParameters().getUsername());
            serviceParameters.setPassword(jiraCredentialsDialog.getUpdatedServiceParameters().getPassword());

            handleUserCredentialsDecision(serviceParameters, jiraCredentialsDialog);

        } //else throw new UserCancelException();
        return serviceParameters;
    }

    public JiraCredentialsDialog createJiraCredentialsDialog(Project project, JiraServiceParameters serviceParameters) {
        return new JiraCredentialsDialog(project, serviceParameters);
    }

    public boolean storeByDefault() {
        return passwordSafeWrapper.isRememberPasswordByDefault();
    }

    public void storeCredentials(JiraServiceParameters serviceParameters) {
        Credentials credentials = new Credentials(serviceParameters.getUsername(), serviceParameters.getPassword());
        passwordSafeWrapper.set(createCredentialAttributes(serviceParameters.getUrl()), credentials);
    }

    public void deleteCredentials(JiraServiceParameters serviceParameters) {
        passwordSafeWrapper.set(createCredentialAttributes(serviceParameters.getUrl()), null);
    }

    private CredentialAttributes createCredentialAttributes(URL jiraUrl) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jira", jiraUrl.toExternalForm()));
    }

    private void handleUserCredentialsDecision(JiraServiceParameters serviceParameters, JiraCredentialsDialog jiraCredentialsDialog) {
        if (jiraCredentialsDialog.storeCredentials()) {
            storeCredentials(serviceParameters);
        } else {
            deleteCredentials(serviceParameters);
        }
    }
}
