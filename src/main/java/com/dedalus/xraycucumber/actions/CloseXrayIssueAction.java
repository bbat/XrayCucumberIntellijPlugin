package com.dedalus.xraycucumber.actions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;

import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.exceptions.UserCancelException;
import com.dedalus.xraycucumber.service.JiraService;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.ServiceParametersUtils;
import com.dedalus.xraycucumber.ui.NotificationUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class CloseXrayIssueAction extends AnAction {

    @Override public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override public void update(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = event.getProject();
        String projectKey;
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();

        try {
            projectKey = serviceParametersUtils.getServiceParameters(project).getProjectKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (virtualFile != null && virtualFile.getName().endsWith(".feature")) {
            Editor editor = event.getData(PlatformDataKeys.EDITOR);
            if (editor != null) {
                String selectedText = editor.getSelectionModel().getSelectedText();
                if (isSelectedTextXrayIssue(selectedText, projectKey)) {
                    event.getPresentation().setEnabledAndVisible(true);
                    return;
                }
            }
        }

        event.getPresentation().setEnabledAndVisible(false);
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        NotificationUtils notificationUtils = new NotificationUtils(project);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        JiraService jiraService;

        try {
            jiraService = new JiraService(getServiceParameters(event));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (editor != null) {
            String xrayIssue = Objects.requireNonNull(editor.getSelectionModel().getSelectedText()).trim().substring(1);

            try {
                if(jiraService.getXrayIssueStatus(xrayIssue).equalsIgnoreCase("ouverte")) {
                    jiraService.closeXrayIssue(xrayIssue);
                    notificationUtils.notifyInfo("This Xray issue " + xrayIssue + " is now closed");
                } else {
                    notificationUtils.notifyInfo("You can close only open status Xray issue");
                }

            } catch (URISyntaxException | AuthenticationException | org.apache.http.auth.AuthenticationException | IOException e) {
                notificationUtils.notifyError(String.valueOf(e));

            }
        }
    }

    private boolean isSelectedTextXrayIssue(final String selectedText, final String projectKey) {
        if (selectedText != null && selectedText.startsWith("@")) {
            String text = selectedText.trim().substring(1);
            String regex = "^" + projectKey + "-(\\d+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            return matcher.matches();
        } else {
            return false;
        }
    }

    private JiraServiceParameters getServiceParameters(@NotNull final AnActionEvent event) throws IOException, UserCancelException {
        final Project project = event.getProject();
        ServiceParametersUtils serviceParametersUtils = new ServiceParametersUtils();
        return serviceParametersUtils.getServiceParameters(project);
    }
}
