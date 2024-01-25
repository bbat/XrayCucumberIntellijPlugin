package com.dedalus.xraycucumber.serviceparameters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.dedalus.xraycucumber.settings.XrayCucumberSettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class ServiceParametersUtils {

    public JiraServiceParameters getServiceParameters(final Project project) throws IOException {
        JiraServiceParameters serviceParameters = getParametersFromSettings();
        serviceParameters = updateServiceParametersWithCredentials(project, serviceParameters);
        return serviceParameters;
    }

    private static JiraServiceParameters updateServiceParametersWithCredentials(final Project project, JiraServiceParameters serviceParameters) {
        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        credentialManager.retrieveCredentialsFromStoreIfUndefined(serviceParameters);

        final JiraServiceParameters[] finalServiceParameters = { serviceParameters };
        String username = finalServiceParameters[0].getUsername();
        String password = finalServiceParameters[0].getPassword();

        boolean isTokenAuthentication = Optional.of(finalServiceParameters[0].isTokenAuthenticationEnabled()).orElse(false);

        if (!isTokenAuthentication) {
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                ApplicationManager.getApplication().invokeAndWait(() ->
                        credentialManager.requestJiraCredentialsFromUser(project, finalServiceParameters[0])
                );
            }
        }

        return new JiraServiceParameters.Builder()
                .url(serviceParameters.getUrl())
                .projectKey(serviceParameters.getProjectKey())
                .tokenAuthenticationEnabled(serviceParameters.isTokenAuthenticationEnabled())
                .saveFeatureBeforeUpdateEnabled(serviceParameters.isSaveFeatureBeforeUpdate())
                .bearerToken(serviceParameters.getBearerToken())
                .username(finalServiceParameters[0].getUsername())
                .password(finalServiceParameters[0].getPassword())
                .build();
    }


    private JiraServiceParameters getParametersFromSettings() throws MalformedURLException {
        XrayCucumberSettingsState xrayCucumberSettingsState = XrayCucumberSettingsState.getInstance();
        assert xrayCucumberSettingsState != null;

        String jiraUrl = xrayCucumberSettingsState.jiraUrl;
        String xrayTestProjectName = xrayCucumberSettingsState.xrayTestProjectName;
        String bearerToken = xrayCucumberSettingsState.bearerToken;
        boolean tokenAuthentication = xrayCucumberSettingsState.tokenAuthentication;
        boolean saveFeatureBeforeUpdate = xrayCucumberSettingsState.saveFeatureBeforeUpd;

        if (jiraUrl == null
                || jiraUrl.isEmpty()
                || xrayTestProjectName == null
                || xrayTestProjectName.isEmpty()
                || bearerToken.isEmpty()) {
            throw new IllegalStateException("Check your settings, Jira URL and test project key should be defined");
        } else {
            return new JiraServiceParameters.Builder()
                    .url(new URL(jiraUrl))
                    .projectKey(xrayTestProjectName)
                    .tokenAuthenticationEnabled(tokenAuthentication)
                    .bearerToken(bearerToken)
                    .saveFeatureBeforeUpdateEnabled(saveFeatureBeforeUpdate)
                    .build();
        }
    }
}