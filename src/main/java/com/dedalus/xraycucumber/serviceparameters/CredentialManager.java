package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;
import java.util.Optional;

import com.dedalus.xraycucumber.ui.dialog.JiraCredentialsDialog;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.project.Project;

public class CredentialManager {

    public JiraServiceParameters retrieveCredentialsFromStoreIfUndefined(JiraServiceParameters serviceParameters) {
        return Optional.ofNullable(PasswordSafe.getInstance().get(createCredentialAttributes(serviceParameters.getUrl())))
                .map(c -> new JiraServiceParameters.Builder()
                        .url(serviceParameters.getUrl())
                        .username(serviceParameters.getUsername() == null ? c.getUserName() : serviceParameters.getUsername())
                        .password(serviceParameters.getPassword() == null ? c.getPasswordAsString() : serviceParameters.getPassword())
                        .projectKey(serviceParameters.getProjectKey())
                        .build())
                .orElse(serviceParameters);
    }

    public JiraServiceParameters requestJiraCredentialsFromUser(Project project, JiraServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = new JiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            serviceParameters = jiraCredentialsDialog.getUpdatedServiceParameters();
            if (jiraCredentialsDialog.storeCredentials()) {
                storeCredentials(serviceParameters);
            } else {
                deleteCredentials(serviceParameters);
            }
            return serviceParameters;
        }
        return null;
    }

    public boolean storeByDefault() {
        return PasswordSafe.getInstance().isRememberPasswordByDefault();
    }

    public void storeCredentials(JiraServiceParameters serviceParameters) {
        Credentials credentials = new Credentials(serviceParameters.getUsername(), serviceParameters.getPassword());
        PasswordSafe.getInstance().set(createCredentialAttributes(serviceParameters.getUrl()), credentials);
    }

    public void deleteCredentials(JiraServiceParameters serviceParameters) {
        PasswordSafe.getInstance().set(createCredentialAttributes(serviceParameters.getUrl()), null);
    }

    private CredentialAttributes createCredentialAttributes(URL jiraUrl) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jira", jiraUrl.toExternalForm()));
    }

}