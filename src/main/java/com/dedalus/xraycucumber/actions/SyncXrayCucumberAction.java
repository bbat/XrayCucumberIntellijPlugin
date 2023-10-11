package com.dedalus.xraycucumber.actions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.AuthenticationException;

import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.gherkin.GherkinFileParser;
import com.dedalus.xraycucumber.gherkin.GherkinFileUpdater;
import com.dedalus.xraycucumber.mapper.JiraXrayIssueMapper;
import com.dedalus.xraycucumber.service.JiraService;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.ServiceParametersUtils;
import com.google.gson.JsonArray;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SyncXrayCucumberAction extends AnAction {

    private JiraServiceParameters jiraServiceParameters;

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
            gherkinFileUpdater.saveBeforeUpdate(featureFile);
            ApplicationManager.getApplication().runWriteAction(() -> {
                Document document = FileDocumentManager.getInstance().getDocument(featureFile);
                Objects.requireNonNull(document);
                FileDocumentManager.getInstance().saveDocument(gherkinFileUpdater.addTagsOnScenario(document, jiraXrayIssueMap, cucumberFeatureIssueMap));

            });
        } catch (URISyntaxException | AuthenticationException | org.apache.http.auth.AuthenticationException | IOException e) {
            System.out.println(e);
        }
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project, jiraServiceParameters);
    }
}