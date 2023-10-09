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

        CredentialManager credentialManager = new CredentialManager();
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);

        if (serviceParameters.getUsername() == null || serviceParameters.getPassword() == null) {
            serviceParameters = credentialManager.requestJiraCredentialsFromUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}
