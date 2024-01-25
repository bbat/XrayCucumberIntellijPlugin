package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;
import java.util.Optional;

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

    public Optional<Credentials> retrieveJiraCredentialsFromStore(URL jiraURL) {
        return Optional.ofNullable(passwordSafeWrapper.get(createCredentialAttributes(jiraURL)));
    }

    public Credentials getJiraCredentialsFromUser(Project project, JiraServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = createJiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            Credentials credentialsFromUser = jiraCredentialsDialog.getCredentialsFromUser();
            handleUserCredentialsDecision(serviceParameters.getUrl(), credentialsFromUser, jiraCredentialsDialog);
            return credentialsFromUser;
        } else
            throw new IllegalStateException("Credentials dialogBox not shown");
    }

    public JiraCredentialsDialog createJiraCredentialsDialog(Project project, JiraServiceParameters serviceParameters) {
        return new JiraCredentialsDialog(project, serviceParameters);
    }

    public boolean storeByDefault() {
        return passwordSafeWrapper.isRememberPasswordByDefault();
    }

    public void storeCredentials(URL url, Credentials credentials) {
        passwordSafeWrapper.set(createCredentialAttributes(url), credentials);
    }

    public void deleteCredentials(URL url) {
        passwordSafeWrapper.set(createCredentialAttributes(url), null);
    }

    private CredentialAttributes createCredentialAttributes(URL jiraUrl) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jira", jiraUrl.toExternalForm()));
    }

    private void handleUserCredentialsDecision(URL url, Credentials credentials, JiraCredentialsDialog jiraCredentialsDialog) {
        if (jiraCredentialsDialog.storeCredentials()) {
            storeCredentials(url, credentials);
        } else {
            deleteCredentials(url);
        }
    }
}
