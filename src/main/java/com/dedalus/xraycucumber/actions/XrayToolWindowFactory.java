package com.dedalus.xraycucumber.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.gherkin.GherkinFileParser;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;

public class XrayToolWindowFactory implements ToolWindowFactory {

    @Override public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        XrayToolWindow myToolWindow;
        try {
            myToolWindow = new XrayToolWindow(project);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myToolWindow.initToolWindow(toolWindow);
    }

    private static class XrayToolWindow {

        private final Project project;
        private final JPanel content;
        private final JBTable scenarioTable;

        public XrayToolWindow(Project project) throws IOException {
            this.project = project;

            content = new SimpleToolWindowPanel(true, true);

            List<String[]> featureData = readFeatureDataFromOpenFile();

            if (featureData.isEmpty()) {
                DefaultTableModel model = new DefaultTableModel(new Object[][] {{"This opened file is not a Cucumber feature"}}, new Object[] {"Message"});
                scenarioTable = new JBTable(model);
                JBScrollPane scrollPane = new JBScrollPane(scenarioTable);
                content.add(scrollPane);
            } else {
                Object[] columnNames = {"Scenario Name", "Status", "Assignee"};
                DefaultTableModel model = new DefaultTableModel(featureData.toArray(new Object[0][]), columnNames);
                scenarioTable = new JBTable(model);
                JBScrollPane scrollPane = new JBScrollPane(scenarioTable);
                content.add(scrollPane);
            }
            addDocumentListener();
        }

        private void addDocumentListener() {
            MessageBusConnection connection = project.getMessageBus().connect();
            connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

                @Override public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                    try {
                        VirtualFile selectedFile = event.getNewFile();
                        if (selectedFile != null) {
                            String extension = selectedFile.getExtension();
                            if ("feature".equalsIgnoreCase(extension)) {
                                updateTable();
                            } else {
                                DefaultTableModel model = new DefaultTableModel(new Object[][] {{"This opened file is not a Cucumber feature"}}, new Object[] {"Message"});
                                scenarioTable.setModel(model);
                                scenarioTable.repaint();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        private void updateTable() throws IOException {
            List<String[]> featureData = readFeatureDataFromOpenFile();
            DefaultTableModel model = new DefaultTableModel(featureData.toArray(new Object[0][]), new Object[] {"Scenario Name", "Status", "Assignee"});
            scenarioTable.setModel(model);
            scenarioTable.repaint();
        }

        public void initToolWindow(ToolWindow toolWindow) {
            toolWindow.getComponent().add(content);
        }

        private List<String[]> readFeatureDataFromOpenFile() {
            List<String[]> featureData = new ArrayList<>();

            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            Editor selectedTextEditor = fileEditorManager.getSelectedTextEditor();
            if (selectedTextEditor != null) {
                Document document = selectedTextEditor.getDocument();
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

                if (psiFile != null) {
                    String filePath = psiFile.getVirtualFile().getPath();

                    if (!filePath.endsWith(".feature")) {
                        return featureData;
                    }

                    GherkinFileParser gherkinFileParser = new GherkinFileParser();
                    try {
                        Map<String, List<String>> scenariosWithTags = gherkinFileParser.getScenariosAndTags(filePath);

                        for (Map.Entry<String, List<String>> entry : scenariosWithTags.entrySet()) {
                            String scenarioName = entry.getKey();
                            List<String> tags = entry.getValue();

                            String status = "Status";
                            String assignee = "Assignee";

                            featureData.add(new String[] {scenarioName, status, assignee});
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return featureData;
        }
    }
}
