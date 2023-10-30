package com.dedalus.xraycucumber.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class CloseXrayIssueAction extends AnAction {

    @Override public void update(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.getName().endsWith(".feature")) {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor != null) {
                String selectedText = editor.getSelectionModel().getSelectedText();
                if (selectedText != null && selectedText.startsWith("@")) {
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                }
            }
        }

        e.getPresentation().setEnabledAndVisible(false);
    }

    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (selectedText != null && selectedText.startsWith("@")) {
                System.out.println("blabla");
            }

        }
    }
}
