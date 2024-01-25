package com.dedalus.xraycucumber.actions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
    JiraServiceParameters jiraServiceParameters;
    Project project;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
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
        FileDocumentManager.getInstance().saveAllDocuments();
        project = event.getProject();

        NotificationUtils notificationUtils = new NotificationUtils(project);
        GherkinFileParser gherkinFileParser = new GherkinFileParser();
        GherkinFileUpdater gherkinFileUpdater = new GherkinFileUpdater();
        JiraXrayIssueMapper jiraXrayIssueMapper = new JiraXrayIssueMapper();
        VirtualFile featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

        try {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Upload feature to Xray") {

                boolean success = false;

                public void run(@NotNull ProgressIndicator progressIndicator) {
                    Objects.requireNonNull(featureFile, "The feature file cannot be null");
                    try {
                        if (synchroStartUserNotification()) {
                            ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils(project);
                            jiraServiceParameters = serviceParametersUtils.getServiceParameters();
                            JiraService jiraService = serviceParametersUtils.getJiraService(jiraServiceParameters);

                            setJiraUploadResponse(jiraService.uploadFeatureToXray(featureFile));
                            setJiraXrayIssueMap(jiraXrayIssueMapper.map(getJiraUploadResponse()));
                            setCucumberFeatureIssueMap(gherkinFileParser.getScenariosAndTags(featureFile.getPath()));
                            success = true;
                        } else {
                            success = false;
                        }
                    } catch (URISyntaxException | IOException | AuthenticationException | org.apache.http.auth.AuthenticationException e) {
                        success = false;
                        notificationUtils.notifyError(String.valueOf(e));
                    }
                }

                @Override
                public void onSuccess() {
                    if (success) {
                        Objects.requireNonNull(featureFile, "The feature file cannot be null");

                        try {
                            if (jiraServiceParameters.isSaveFeatureBeforeUpdate()) {
                                gherkinFileUpdater.saveBeforeUpdate(featureFile);
                            }

                            ApplicationManager.getApplication().runWriteAction(() -> {
                                Document document = FileDocumentManager.getInstance().getDocument(featureFile);
                                Document documentUpdated = gherkinFileUpdater.addXrayIssueIdTagsOnScenario(document, getJiraXrayIssueMap(), getCucumberFeatureIssueMap());

                                FileDocumentManager.getInstance().saveDocument(documentUpdated);
                                reformatCode(event, documentUpdated);
                            });

                            notificationUtils.notifySuccess("This feature file is now synchronized with Xray");

                        } catch (IOException e) {
                            notificationUtils.notifyError(String.valueOf(e));
                        }

                    }
                }
            });
        } catch (UserCancelException e) {
            notificationUtils.notifyInfo("Action was cancelled by the user");
        }
    }

    private boolean synchroStartUserNotification() {
        final AtomicBoolean success = new AtomicBoolean(false);
        NotificationUtils notificationUtils = new NotificationUtils(this.project);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            SynchroStartPopup popup = new SynchroStartPopup(this.project);
            popup.showAndGet();
            if (popup.isCanceled()) {
                notificationUtils.notifyInfo("User cancellation");
                success.set(false);
            } else
                success.set(true);
        });
        return success.get();
    }

    private Map<String, List<String>> getCucumberFeatureIssueMap() {
        return cucumberFeatureIssueMap;
    }

    private void setCucumberFeatureIssueMap(final Map<String, List<String>> cucumberFeatureIssueMap) {
        this.cucumberFeatureIssueMap = cucumberFeatureIssueMap;
    }

    private JsonArray getJiraUploadResponse() {
        return jiraUploadResponse;
    }

    private void setJiraUploadResponse(final JsonArray jiraUploadResponse) {
        this.jiraUploadResponse = jiraUploadResponse;
    }

    private Map<String, String> getJiraXrayIssueMap() {
        return jiraXrayIssueMap;
    }

    private void setJiraXrayIssueMap(final Map<String, String> jiraXrayIssueMap) {
        this.jiraXrayIssueMap = jiraXrayIssueMap;
    }

    private void reformatCode(final @NotNull AnActionEvent event, final Document document) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        assert psiFile != null;
        new ReformatCodeProcessor(psiFile, false).run();
        PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
    }
}