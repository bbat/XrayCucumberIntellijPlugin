package com.dedalus.xraycucumber.serviceparameters;

import java.io.IOException;

import com.dedalus.xraycucumber.ui.dialog.ServiceParametersDialog;
import com.intellij.openapi.project.Project;

/**
 * Utility class for managing service parameters, specifically Jira service parameters.
 */
public class ServiceParametersUtils {

    /**
     * Retrieves and manages Jira service parameters, facilitating user interaction
     * and credential management.
     * The method first displays a dialog to get service parameters from the user.
     * Then, credentials (username and password) are retrieved from the credential
     * manager or requested from the user, ensuring that valid credentials are always
     * available or purposely user-aborted.
     *
     * @param project            the current project, used to interact with UI components.
     * @param serviceParameters  the initial service parameters, to be managed and verified.
     * @return JiraServiceParameters, fully populated with data either entered or retrieved.
     * @throws IOException if any I/O error occurs during parameter retrieval or management.
     */
    public JiraServiceParameters getServiceParameters(final Project project, JiraServiceParameters serviceParameters) throws IOException {
        ServiceParametersDialog serviceParametersDialog = new ServiceParametersDialog(project);
        if (serviceParametersDialog.showAndGet()) {
            serviceParameters = serviceParametersDialog.createServiceParameters();
        }

        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);
        if (serviceParameters.getUsername().isEmpty() || serviceParameters.getPassword().isEmpty()) {
            serviceParameters = credentialManager.requestJiraCredentialsFromUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}