package com.dedalus.xraycucumber.serviceparameters;

import java.io.IOException;

import com.dedalus.xraycucumber.ui.dialog.ServiceParametersDialog;
import com.intellij.openapi.project.Project;

public class ServiceParametersUtils {

    public JiraServiceParameters getServiceParameters(final Project project, JiraServiceParameters serviceParameters) throws IOException {
        ServiceParametersDialog serviceParametersDialog = new ServiceParametersDialog(project);
        if (serviceParametersDialog.showAndGet()) {
            serviceParameters = serviceParametersDialog.createServiceParameters();
        }

        //PasswordSafeWrapper passwordSafeWrapper = new PasswordSafeWrapper();
        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);
        if (serviceParameters.getUsername().isEmpty() || serviceParameters.getPassword().isEmpty()) {
            serviceParameters = credentialManager.requestJiraCredentialsFromUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}