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

/**
 * Synchronizes Gherkin feature files with Xray through JIRA integration.
 * <p>
 * This action triggers the synchronization of a local Gherkin feature file
 * with associated tests in Xray via JIRA, ensuring that test definitions in JIRA
 * reflect the current state of the local feature file. The synchronization process
 * involves the following main steps:
 * </p>
 * <ol>
 *     <li>Uploading the feature file to Xray through JIRA's API.</li>
 *     <li>Mapping of JIRA Xray issues to scenarios in the feature file.</li>
 *     <li>Updating the feature file with tags correlating to JIRA issues.</li>
 *     <li>Backup and update of the local feature file with new/modified tags.</li>
 * </ol>
 **/
public class SyncXrayCucumberAction extends AnAction {

    private JiraServiceParameters jiraServiceParameters;

    @Override public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override public void update(AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = file != null && file.getName().endsWith(".feature");
        event.getPresentation().setEnabledAndVisible(visible);
    }

    /**
     * Executes the action of synchronizing the local feature file with Xray via JIRA.
     * <p>
     * Triggered when the user performs the associated UI action,
     * initiating the synchronization process, which includes uploading the feature file to JIRA,
     * parsing and mapping issues and scenarios, and updating the local file accordingly.
     * </p>
     * <p>
     * Error handling and user notifications are provided to ensure smooth user experience
     * and alerting in case of synchronization failures.
     * </p>
     *
     * @param event Event related to the performed action, carrying data about it.
     */
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
            checkFeatureFileValidity(featureFile);

            jiraServiceParameters = getServiceParameters(event);
            JsonArray jiraUploadResponse = new JiraService(jiraServiceParameters).uploadFeatureToXray(featureFile);

            jiraXrayIssueMap = jiraXrayIssueMapper.map(jiraUploadResponse);
            cucumberFeatureIssueMap = gherkinFileParser.getScenariosAndTags(featureFile);

            gherkinFileUpdater.saveBeforeUpdate(featureFile);

            ApplicationManager.getApplication().runWriteAction(() -> {
                Document document = FileDocumentManager.getInstance().getDocument(featureFile);
                Objects.requireNonNull(document);
                Document documentUpdated = gherkinFileUpdater.addTagsOnScenario(document, jiraXrayIssueMap, cucumberFeatureIssueMap);

                FileDocumentManager.getInstance().saveDocument(documentUpdated);
            });

            notificationUtils.notifySuccess("This feature file is now synchronized with Xray");

        } catch (UserCancelException e) {
            notificationUtils.notifyInfo("Action was cancelled by the user");

        }catch (URISyntaxException | AuthenticationException | org.apache.http.auth.AuthenticationException | IOException e) {
            notificationUtils.notifyError(String.valueOf(e));

        }
    }

    @NotNull private static GherkinFileParser checkFeatureFileValidity(final VirtualFile featureFile) {
        assert featureFile != null;
        GherkinFileParser gherkinFileParser = new GherkinFileParser();
        gherkinFileParser.verify(featureFile);
        return gherkinFileParser;
    }

    private static void synchroStartUserNotification(final Project project) {
        SynchroStartPopup popup = new SynchroStartPopup(project);
        if(!popup.show()) {
            throw new UserCancelException();
        }
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException, UserCancelException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project);
    }
}