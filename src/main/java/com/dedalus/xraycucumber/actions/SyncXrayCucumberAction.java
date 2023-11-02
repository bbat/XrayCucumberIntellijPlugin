package com.dedalus.xraycucumber.actions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.AuthenticationException;

import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.exceptions.UserCancelException;
import com.dedalus.xraycucumber.gherkin.GherkinFileParser;
import com.dedalus.xraycucumber.gherkin.GherkinFileUpdater;
import com.dedalus.xraycucumber.mapper.JiraXrayIssueMapper;
import com.dedalus.xraycucumber.service.JiraService;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.ServiceParametersUtils;
import com.dedalus.xraycucumber.ui.NotificationUtils;
import com.dedalus.xraycucumber.ui.SynchroStartPopup;
import com.google.gson.JsonArray;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SyncXrayCucumberAction extends AnAction {

    private static void synchroStartUserNotification(final Project project) {
        SynchroStartPopup popup = new SynchroStartPopup(project);
        if (!popup.show()) {
            throw new UserCancelException();
        }
    }

    @Override public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override public void update(AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = file != null && file.getName().endsWith(".feature");
        event.getPresentation().setEnabledAndVisible(visible);
    }

    @Override public void actionPerformed(@NotNull final AnActionEvent event) {
        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = event.getProject();

        NotificationUtils notificationUtils = new NotificationUtils(project);
        GherkinFileParser gherkinFileParser = new GherkinFileParser();
        GherkinFileUpdater gherkinFileUpdater = new GherkinFileUpdater();
        JiraXrayIssueMapper jiraXrayIssueMapper = new JiraXrayIssueMapper();

        Map<String, List<String>> cucumberFeatureIssueMap;
        Map<String, String> jiraXrayIssueMap;

        VirtualFile featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

        try {
            synchroStartUserNotification(project);
            Objects.requireNonNull(featureFile, "The feature file cannot be null");
            JsonArray jiraUploadResponse = new JiraService(getServiceParameters(event)).uploadFeatureToXray(featureFile);
            jiraXrayIssueMap = jiraXrayIssueMapper.map(jiraUploadResponse);

            cucumberFeatureIssueMap = gherkinFileParser.getScenariosAndTags(featureFile.getPath());

            gherkinFileUpdater.saveBeforeUpdate(featureFile);

            ApplicationManager.getApplication().runWriteAction(() -> {
                Document document = FileDocumentManager.getInstance().getDocument(featureFile);
                Document documentUpdated = gherkinFileUpdater.addXrayIssueIdTagsOnScenario(document, jiraXrayIssueMap, cucumberFeatureIssueMap);

                FileDocumentManager.getInstance().saveDocument(documentUpdated);
            });

            notificationUtils.notifySuccess("This feature file is now synchronized with Xray");

        } catch (UserCancelException e) {
            notificationUtils.notifyInfo("Action was cancelled by the user");

        } catch (URISyntaxException | AuthenticationException | org.apache.http.auth.AuthenticationException | IOException e) {
            notificationUtils.notifyError(String.valueOf(e));

        }
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException, UserCancelException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project);
    }
}