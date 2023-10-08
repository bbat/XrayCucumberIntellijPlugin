package com.dedalus.xraycucumber.ui.utils;

import java.io.IOException;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.ui.dialog.ServiceParametersDialog;
import com.intellij.openapi.project.Project;

public class ServiceParametersUtils {

    public ServiceParameters getServiceParameters(final Project project, ServiceParameters serviceParameters) throws IOException {
        ServiceParametersDialog serviceParametersDialog = new ServiceParametersDialog(project, serviceParameters);
        if (serviceParametersDialog.showAndGet()) {
            serviceParameters = serviceParametersDialog.createServiceParameters();
        }

        CredentialManager credentialManager = new CredentialManager();
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);

        if (serviceParameters.getUsername() == null || serviceParameters.getPassword() == null) {
            serviceParameters = credentialManager.requestJiraCredentialsFormUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}
