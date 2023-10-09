package com.dedalus.xraycucumber.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.mapper.JiraXrayIssueMapper;
import com.dedalus.xraycucumber.model.JiraServiceParameters;
import com.dedalus.xraycucumber.service.JiraService;
import com.dedalus.xraycucumber.ui.utils.ServiceParametersUtils;
import com.dedalus.xraycucumber.utils.gherkin.GherkinFileParser;
import com.dedalus.xraycucumber.utils.gherkin.GherkinFileUpdater;
import com.google.gson.JsonArray;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SyncXrayCucumberAction extends AnAction {
    private JiraServiceParameters jiraServiceParameters;

    @Override public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = file != null && file.getName().endsWith(".feature");
        event.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        try {
            FileDocumentManager.getInstance().saveAllDocuments();
            VirtualFile featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

            jiraServiceParameters = getServiceParameters(event);

            assert featureFile != null;
            JsonArray jiraUploadResponse = new JiraService(jiraServiceParameters).uploadFeatureToXray(featureFile);

            JiraXrayIssueMapper jiraXrayIssueMapper = new JiraXrayIssueMapper();
            Map<String, String> jiraXrayIssueMap = jiraXrayIssueMapper.map(jiraUploadResponse);

            Map<String, List<String>> cucumberFeatureIssueMap = new GherkinFileParser().getScenariosAndTags(featureFile);

            GherkinFileUpdater gherkinFileUpdater = new GherkinFileUpdater();
            gherkinFileUpdater.addTagsOnScenario(featureFile, jiraXrayIssueMap, cucumberFeatureIssueMap);

        } catch (IOException | URISyntaxException | AuthenticationException | org.apache.http.auth.AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project, jiraServiceParameters);
    }
}