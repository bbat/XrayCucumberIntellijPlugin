package com.dedalus.xraycucumber.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(
        name = "com.dedalus.xraycucumber.configuration.XrayCucumberSettingState",
        storages = {@Storage("XrayCucumberPluginSettings.xml")}
)
public class XrayCucumberSettingsState implements PersistentStateComponent<XrayCucumberSettingsState> {
    public String jiraUrl;
    public String xrayTestProjectName;

    @Nullable @Override public XrayCucumberSettingsState getState() {
        return this;
    }

    @Override public void loadState(@NotNull final XrayCucumberSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Nullable
    public static XrayCucumberSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(XrayCucumberSettingsState.class);
    }
}
