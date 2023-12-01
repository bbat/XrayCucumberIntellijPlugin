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
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class SyncXrayCucumberAction extends AnAction {
    JsonArray jiraUploadResponse;
    Map<String, List<String>> cucumberFeatureIssueMap;
    Map<String, String> jiraXrayIssueMap;

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
        VirtualFile featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

        synchroStartUserNotification(project);

        try {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Upload feature to Xray") {
                public void run(@NotNull ProgressIndicator progressIndicator) {
                  Objects.requireNonNull(featureFile, "The feature file cannot be null");
                  try {
                      setJiraUploadResponse(new JiraService(getServiceParameters(event)).uploadFeatureToXray(featureFile));
                      setJiraXrayIssueMap(jiraXrayIssueMapper.map(getJiraUploadResponse()));
                      setCucumberFeatureIssueMap(gherkinFileParser.getScenariosAndTags(featureFile.getPath()));

                  } catch (URISyntaxException | IOException | AuthenticationException | org.apache.http.auth.AuthenticationException e) {
                      notificationUtils.notifyError(String.valueOf(e));
                  }
                }

                @Override
                public void onSuccess() {
                    Objects.requireNonNull(featureFile, "The feature file cannot be null");
                    ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
                    JiraServiceParameters serviceParameters;

                    try {
                        serviceParameters = serviceParametersUtils.getServiceParameters(project);

                        if(serviceParameters.isSaveFeatureBeforeUpdate()) {
                            gherkinFileUpdater.saveBeforeUpdate(featureFile);
                        }

                        ApplicationManager.getApplication().runWriteAction(() -> {
                            Document document = FileDocumentManager.getInstance().getDocument(featureFile);
                            Document documentUpdated = gherkinFileUpdater.addXrayIssueIdTagsOnScenario(document, getJiraXrayIssueMap(), getCucumberFeatureIssueMap());

                            FileDocumentManager.getInstance().saveDocument(documentUpdated);
                            reformatCode(event, project, documentUpdated);
                        });

                        notificationUtils.notifySuccess("This feature file is now synchronized with Xray");

                    } catch (IOException e) {
                        notificationUtils.notifyError(String.valueOf(e));
                    }
                }
            });
        } catch (UserCancelException e) {
            notificationUtils.notifyInfo("Action was cancelled by the user");
        }
    }

    public Map<String, List<String>> getCucumberFeatureIssueMap() {
        return cucumberFeatureIssueMap;
    }

    public JsonArray getJiraUploadResponse() {
        return jiraUploadResponse;
    }

    public void setJiraUploadResponse(final JsonArray jiraUploadResponse) {
        this.jiraUploadResponse = jiraUploadResponse;
    }
    public void setCucumberFeatureIssueMap(final Map<String, List<String>> cucumberFeatureIssueMap) {
        this.cucumberFeatureIssueMap = cucumberFeatureIssueMap;
    }

    public Map<String, String> getJiraXrayIssueMap() {
        return jiraXrayIssueMap;
    }

    public void setJiraXrayIssueMap(final Map<String, String> jiraXrayIssueMap) {
        this.jiraXrayIssueMap = jiraXrayIssueMap;
    }

    private static void reformatCode(final @NotNull AnActionEvent event, final Project project, final Document document) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        assert psiFile != null;
        new ReformatCodeProcessor(psiFile,false).run();
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException, UserCancelException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project);
    }
}