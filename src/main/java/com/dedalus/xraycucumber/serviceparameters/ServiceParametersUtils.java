package com.dedalus.xraycucumber.serviceparameters;

import java.io.IOException;

import com.dedalus.xraycucumber.exceptions.UserCancelException;
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
     * @param project the current project, used to interact with UI components.
     * @return JiraServiceParameters, fully populated with data either entered or retrieved.
     * @throws IOException if any I/O error occurs during parameter retrieval or management.
     */
    public JiraServiceParameters getServiceParameters(final Project project) throws IOException {
        ServiceParametersDialog serviceParametersDialog = new ServiceParametersDialog(project);
        JiraServiceParameters serviceParameters;
        if (serviceParametersDialog.showAndGet()) {
            serviceParameters = serviceParametersDialog.createServiceParameters();
        } else {
            throw new UserCancelException();
        }

        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);

        if (serviceParameters.getUsername()==null
                || serviceParameters.getUsername().isEmpty()
                || serviceParameters.getPassword()==null
                || serviceParameters.getPassword().isEmpty()) {
            serviceParameters = credentialManager.requestJiraCredentialsFromUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}