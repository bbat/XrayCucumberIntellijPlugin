package com.dedalus.xraycucumber.serviceparameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.plexus.util.StringUtils;

import com.dedalus.xraycucumber.service.JiraService;
import com.dedalus.xraycucumber.settings.XrayCucumberSettingsState;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class ServiceParametersUtils {

    Project project;

    public ServiceParametersUtils(Project project) {
        this.project = project;
    }

    public JiraServiceParameters getServiceParameters() throws MalformedURLException {
        XrayCucumberSettingsState xrayCucumberSettingsState = XrayCucumberSettingsState.getInstance();
        assert xrayCucumberSettingsState != null;

        String jiraUrl = xrayCucumberSettingsState.jiraUrl;
        String xrayTestProjectName = xrayCucumberSettingsState.xrayTestProjectName;
        String bearerToken = xrayCucumberSettingsState.bearerToken;
        boolean tokenAuthentication = xrayCucumberSettingsState.tokenAuthentication;
        boolean saveFeatureBeforeUpdate = xrayCucumberSettingsState.saveFeatureBeforeUpd;

        if (tokenAuthentication) {
            if (StringUtils.isEmpty(bearerToken)) {
                throw new IllegalStateException("Check your settings, bearer token should be defined");
            }
        }

        if (StringUtils.isEmpty(jiraUrl) || StringUtils.isEmpty(xrayTestProjectName)) {
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

    public Credentials getUserCredentials(Project project, JiraServiceParameters serviceParameters) {
        CredentialManager credentialManager = new CredentialManager(new PasswordSafeWrapper());
        Optional<Credentials> credentials = credentialManager.retrieveJiraCredentialsFromStore(serviceParameters.getUrl());
        if (credentials.isEmpty()) {
            final AtomicReference<Credentials> credentialsFromUser = new AtomicReference<>();
            ApplicationManager.getApplication().invokeAndWait(() ->
                    credentialsFromUser.set(credentialManager.getJiraCredentialsFromUser(project, serviceParameters))
            );
            return credentialsFromUser.get();
        } else {
            return credentials.get();
        }
    }

    public JiraService getJiraService(JiraServiceParameters jiraServiceParameters) {
        JiraService jiraService;
        if (jiraServiceParameters.isTokenAuthenticationEnabled()) {
            jiraService = new JiraService(jiraServiceParameters);
        } else {
            Credentials credentials = getUserCredentials(project, jiraServiceParameters);
            jiraService = new JiraService(jiraServiceParameters, credentials);
        }
        return jiraService;
    }
}