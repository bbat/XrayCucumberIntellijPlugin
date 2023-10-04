package com.dedalus.xraycucumber.ui.utils;

import java.io.IOException;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ServiceParametersUtils {

    public static final String XRAY_CUCUMBER_JSON = "xray-cucumber.json";

    public static ServiceParameters getServiceParameters(final Project project, final VirtualFile serviceParametersFile) throws IOException {
        FileServiceParametersLoader loader = new FileServiceParametersLoader();
        ServiceParameters serviceParameters = loader.load(serviceParametersFile);

        CredentialManager credentialManager = new CredentialManager();
        serviceParameters = credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);

        if (serviceParameters.getUsername() == null || serviceParameters.getPassword() == null) {
            serviceParameters = credentialManager.requestJiraCredentialsFormUser(project, serviceParameters);
        }
        return serviceParameters;
    }
}
