package com.dedalus.xraycucumber.ui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.ui.dialog.JiraCredentialsDialog;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ServiceParametersUtils {

    public static final String XRAY_CUCUMBER_JSON = "xray-cucumber.json";

    public static ServiceParameters prepareServiceParameters(final Project project, final VirtualFile serviceParametersFile) throws IOException {
        ServiceParameters serviceParameters = load(serviceParametersFile);
        serviceParameters = ServiceParametersUtils.retrieveCredentialsFromStoreIfUndefined(serviceParameters);
        if (serviceParameters.getUsername() == null || serviceParameters.getPassword() == null) {
            serviceParameters = requestJiraCredentialsFormUser(project, serviceParameters);
        }
        return serviceParameters;
    }

    private static ServiceParameters load(VirtualFile serviceParametersFile) throws IOException {
        try (InputStream inputStream = serviceParametersFile.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
            return objectMapper.readValue(inputStream, ServiceParameters.class);
        }
    }

    private static ServiceParameters retrieveCredentialsFromStoreIfUndefined(ServiceParameters serviceParameters) {
        return Optional.ofNullable(PasswordSafe.getInstance().get(createCredentialAttributes(serviceParameters.getUrl())))
                .map(c -> new ServiceParameters.Builder()
                        .url(serviceParameters.getUrl())
                        .username(serviceParameters.getUsername() == null ? c.getUserName() : serviceParameters.getUsername())
                        .password(serviceParameters.getPassword() == null ? c.getPasswordAsString() : serviceParameters.getPassword())
                        .projectKey(serviceParameters.getProjectKey())
                        .filterId(serviceParameters.getFilterId())
                        .build())
                .orElse(serviceParameters);
    }

    private static CredentialAttributes createCredentialAttributes(URL jiraUrl) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jira", jiraUrl.toExternalForm()));
    }

    private static ServiceParameters requestJiraCredentialsFormUser(Project project, ServiceParameters serviceParameters) {
        JiraCredentialsDialog jiraCredentialsDialog = new JiraCredentialsDialog(project, serviceParameters);
        if (jiraCredentialsDialog.showAndGet()) {
            serviceParameters = jiraCredentialsDialog.getUpdatedServiceParameters();
            if (jiraCredentialsDialog.storeCredentials()) {
                ServiceParametersUtils.storeCredentials(serviceParameters);
            } else {
                ServiceParametersUtils.deleteCredentials(serviceParameters);
            }
            return serviceParameters;
        }
        return null;
    }

    public static boolean storeByDefault() {
        return PasswordSafe.getInstance().isRememberPasswordByDefault();
    }

    public static void storeCredentials(ServiceParameters serviceParameters) {
        Credentials credentials = new Credentials(serviceParameters.getUsername(), serviceParameters.getPassword());
        PasswordSafe.getInstance().set(createCredentialAttributes(serviceParameters.getUrl()), credentials);
    }

    public static void deleteCredentials(ServiceParameters serviceParameters) {
        PasswordSafe.getInstance().set(createCredentialAttributes(serviceParameters.getUrl()), null);
    }
}
