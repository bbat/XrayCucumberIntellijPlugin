package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;
import java.util.Optional;

import com.dedalus.xraycucumber.ui.dialog.JiraCredentialsDialog;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.project.Project;

/**
 * Manages the storage, retrieval, and user interaction for Jira credentials.
 */
public class CredentialManager {

    private final PasswordSafeWrapper passwordSafeWrapper;

    /**
     * Constructs a new CredentialManager with a specified PasswordSafeWrapper.
     *
     * @param passwordSafeWrapper the wrapper for interacting with the IDE's password safe.
     */
    public CredentialManager(PasswordSafeWrapper passwordSafeWrapper) {
        this.passwordSafeWrapper = passwordSafeWrapper;
    }

    /**
     * Retrieves credentials from the secure store or uses provided credentials if they are defined.
     *
     * @param serviceParameters the initial service parameters, which may have null username or password.
     * @return updated service parameters with credentials filled in from the secure store if they were not provided.
     */
    public JiraServiceParameters retrieveCredentialsFromStoreIfUndefined(JiraServiceParameters serviceParameters) {
        return Optional.ofNullable(passwordSafeWrapper.get(createCredentialAttributes(serviceParameters.getUrl())))
                .map(c -> new JiraServiceParameters.Builder().url(serviceParameters.getUrl()).username(serviceParameters.getUsername() == null ? c.getUserName() : serviceParameters.getUsername())
                        .password(serviceParameters.getPassword() == null ? c.getPasswordAsString() : serviceParameters.getPassword()).projectKey(serviceParameters.getProjectKey()).build()).orElse(serviceParameters);
    }

    /**
     * Prompts the user for Jira credentials and updates the service parameters accordingly.
     *
     * @param project            the active IntelliJ project.
     * @param serviceParameters  the initial service parameters.
     * @return updated service parameters with the user-provided credentials.
     */
    public JiraServiceParameters requestJiraCredentialsFromUser(Project project, JiraServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = createJiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            serviceParameters = jiraCredentialsDialog.getUpdatedServiceParameters();
            handleUserCredentialsDecision(serviceParameters, jiraCredentialsDialog);
            return serviceParameters;
        }
        return null;
    }

    /**
     * Creates a dialog for the user to input their Jira credentials.
     *
     * @param project           the active IntelliJ project.
     * @param serviceParameters the initial service parameters.
     * @return a new instance of JiraCredentialsDialog.
     */
    public JiraCredentialsDialog createJiraCredentialsDialog(Project project, JiraServiceParameters serviceParameters) {
        return new JiraCredentialsDialog(project, serviceParameters);
    }

    /**
     * Indicates whether to store the credentials by default.
     *
     * @return true if credentials should be stored by default; false otherwise.
     */
    public boolean storeByDefault() {
        return passwordSafeWrapper.isRememberPasswordByDefault();
    }

    /**
     * Stores the specified Jira credentials securely.
     *
     * @param serviceParameters the service parameters containing the credentials to store.
     */
    public void storeCredentials(JiraServiceParameters serviceParameters) {
        Credentials credentials = new Credentials(serviceParameters.getUsername(), serviceParameters.getPassword());
        passwordSafeWrapper.set(createCredentialAttributes(serviceParameters.getUrl()), credentials);
    }

    /**
     * Deletes the stored credentials associated with the specified Jira URL.
     *
     * @param serviceParameters the service parameters containing the URL of the credentials to delete.
     */
    public void deleteCredentials(JiraServiceParameters serviceParameters) {
        passwordSafeWrapper.set(createCredentialAttributes(serviceParameters.getUrl()), null);
    }

    /**
     * Creates the attributes used to store and retrieve credentials from the password safe.
     *
     * @param jiraUrl the URL of the Jira instance.
     * @return credential attributes configured with the Jira URL.
     */
    private CredentialAttributes createCredentialAttributes(URL jiraUrl) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jira", jiraUrl.toExternalForm()));
    }

    /**
     * Updates the stored credentials based on user's decision in the dialog.
     *
     * @param serviceParameters       the updated service parameters containing the user's credentials.
     * @param jiraCredentialsDialog   the dialog from which to retrieve the user's decision to store credentials.
     */
    private void handleUserCredentialsDecision(JiraServiceParameters serviceParameters, JiraCredentialsDialog jiraCredentialsDialog) {
        if (jiraCredentialsDialog.storeCredentials()) {
            storeCredentials(serviceParameters);
        } else {
            deleteCredentials(serviceParameters);
        }
    }
}
