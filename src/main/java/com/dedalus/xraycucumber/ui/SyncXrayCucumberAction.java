package com.dedalus.xraycucumber.ui;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.XrayCucumberService;
import com.dedalus.xraycucumber.ui.utils.ServiceParametersUtils;
import com.dedalus.xraycucumber.utils.gherkin.GherkinFileUpdater;
import com.google.gson.JsonArray;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SyncXrayCucumberAction extends AnAction {

    public static final String TITLE = "Cucumber Test And Jira Xray Synchronization";
    private VirtualFile featureFile;
    private ServiceParameters serviceParameters;

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
        final Project project = event.getProject();
        FileDocumentManager.getInstance().saveAllDocuments();
        initFeatureFile(event);
        try {
            initServiceParameters(event);
        } catch (Exception e) {
            throw new RuntimeException("Service Initilization Failure: " + e.getStackTrace());
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, TITLE) {

            public void run(@NotNull ProgressIndicator progressIndicator) {
                XrayCucumberService xrayCucumberService = new XrayCucumberService(HttpClients.createDefault(), serviceParameters);
                ProgressIndicatorAdapter progressReporter = new ProgressIndicatorAdapter(progressIndicator, project);
                GherkinFileUpdater gherkinFileUpdater = new GherkinFileUpdater();

                JsonArray issues = xrayCucumberService.uploadXrayCucumberTest(Paths.get(featureFile.getPath()), progressReporter);
                progressReporter.reportProgress("Update Feature file with Xray test case Id", 50);

                try {
                    gherkinFileUpdater.update(featureFile, issues);
                    progressReporter.reportProgress("Update Feature file with Xray test case Id", 100);
                    progressReporter.reportSuccess("Feature file has been updated");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                IssueDetailsDisplayer issueDetailsDisplayer = new IssueDetailsDisplayer(project);
                issueDetailsDisplayer.showInPopup(issues);
            }
        });
    }

    private void initFeatureFile(@NotNull final AnActionEvent event) {
        featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    }

    private void initServiceParameters(@NotNull final AnActionEvent event) throws IOException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        serviceParameters = serviceParametersUtils.getServiceParameters(project, serviceParameters);
    }
}