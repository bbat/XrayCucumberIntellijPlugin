package com.dedalus.xraycucumber.settings;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.dedalus.xraycucumber.ui.XrayCucumberPluginSettingsComponent;
import com.intellij.openapi.options.*;

public class XrayCucumberSettingsConfigurable implements Configurable {
    private XrayCucumberPluginSettingsComponent xrayCucumberPluginSettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Xray Cucumber Plugin Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return xrayCucumberPluginSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        xrayCucumberPluginSettingsComponent = new XrayCucumberPluginSettingsComponent();
        return xrayCucumberPluginSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        XrayCucumberSettingsState settings = XrayCucumberSettingsState.getInstance();
        assert settings != null;
        boolean modified = !xrayCucumberPluginSettingsComponent.getJiraUrlField().equals(settings.jiraUrl);
        modified |= !xrayCucumberPluginSettingsComponent.getXrayTestProjectField().equals(settings.xrayTestProjectName);
        modified |= !xrayCucumberPluginSettingsComponent.getBearerTokenField().equals(settings.bearerToken);
        modified |= !xrayCucumberPluginSettingsComponent.isTokenAuthenticationCheckBoxSelected() == settings.tokenAuthentication;
        return modified && isValid();
    }

    @Override
    public void apply() {
        XrayCucumberSettingsState settings = XrayCucumberSettingsState.getInstance();
        assert settings != null;
        if(isValid()) {
            settings.jiraUrl = xrayCucumberPluginSettingsComponent.getJiraUrlField();
            settings.xrayTestProjectName = xrayCucumberPluginSettingsComponent.getXrayTestProjectField();
            settings.bearerToken = xrayCucumberPluginSettingsComponent.getBearerTokenField();
            settings.tokenAuthentication = xrayCucumberPluginSettingsComponent.isTokenAuthenticationCheckBoxSelected();
        }
    }

    @Override
    public void reset() {
        XrayCucumberSettingsState settings = XrayCucumberSettingsState.getInstance();
        assert settings != null;
        xrayCucumberPluginSettingsComponent.setJiraUrlField(settings.jiraUrl);
        xrayCucumberPluginSettingsComponent.setXrayTestProjectField(settings.xrayTestProjectName);
        xrayCucumberPluginSettingsComponent.setBearerTokenField(settings.bearerToken);
        xrayCucumberPluginSettingsComponent.setTokenAuthenticationCheckBoxSelected(settings.tokenAuthentication);
    }

    @Override
    public void disposeUIResources() {
        xrayCucumberPluginSettingsComponent = null;
    }

    private boolean isValid() {
        return !xrayCucumberPluginSettingsComponent.getJiraUrlField().isEmpty()
                && !xrayCucumberPluginSettingsComponent.getXrayTestProjectField().isEmpty()
                && !xrayCucumberPluginSettingsComponent.getBearerTokenField().isEmpty();
    }
}
