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
        return Optional.ofNullable(passwordSafeWrapper.get(createCredentialAttributes(serviceParameters.getUrl())))
                .map(c -> new JiraServiceParameters.Builder()
                        .url(serviceParameters.getUrl())
                        .username(serviceParameters.getUsername() == null ? c.getUserName() : serviceParameters.getUsername())
                        .password(serviceParameters.getPassword() == null ? c.getPasswordAsString() : serviceParameters.getPassword())
                        .projectKey(serviceParameters.getProjectKey())
                        .bearerToken(serviceParameters.getBearerToken())
                        .tokenAuthenticationEnabled(serviceParameters.isTokenAuthenticationEnabled())
                        .build()).orElse(serviceParameters);
    }

    public JiraServiceParameters requestJiraCredentialsFromUser(Project project, JiraServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = createJiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            serviceParameters = jiraCredentialsDialog.getUpdatedServiceParameters();
            handleUserCredentialsDecision(serviceParameters, jiraCredentialsDialog);
            return serviceParameters;
        } else throw new UserCancelException();
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
