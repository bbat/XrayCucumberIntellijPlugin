package com.dedalus.xraycucumber.utils.gherkin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

public class GherkinFileUpdater {

    private static void saveBeforeUpdate(final VirtualFile featureFile) throws IOException {
        try {
            Path originalPath = Paths.get(featureFile.getPath());
            String originalNameWithoutExtension = originalPath.getFileName().toString().replace(".feature", "");
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String backupFileName = originalNameWithoutExtension + "_Backup-" + timeStamp + ".feature";
            Path backupPath = originalPath.getParent().resolve(backupFileName);
            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public void addTagsOnScenario(VirtualFile featureFile, final Map<String, String> jiraXrayIssueMap, final Map<String, List<String>> cucumberFeatureIssueMap) throws IOException {
        saveBeforeUpdate(featureFile);

        ApplicationManager.getApplication().runWriteAction(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(featureFile);

            if (document != null) {
                StringBuilder newContent = new StringBuilder();

                for (int i = 0; i < document.getLineCount(); i++) {
                    int startOffset = document.getLineStartOffset(i);
                    int endOffset = document.getLineEndOffset(i);

                    String line = document.getText().substring(startOffset, endOffset);

                    if (line.trim().toLowerCase().startsWith("scenario:") || line.trim().toLowerCase().startsWith("scenario outline:")) {
                        String scenarioName = extractScenarioName(line);
                        String jiraXrayIssueId = jiraXrayIssueMap.get(scenarioName);

                        if (shouldAddJiraId(cucumberFeatureIssueMap, scenarioName, jiraXrayIssueId)) {
                            newContent.append("@").append(jiraXrayIssueId).append("\n");
                        }
                    }
                    newContent.append(line).append("\n");
                }

                document.setText(newContent.toString());

                FileDocumentManager.getInstance().saveDocument(document);

            }

        });
    }

    private String extractScenarioName(String line) {
        if (line.toLowerCase().contains("outline:")) {
            return line.trim().substring(17).trim();
        } else if (line.toLowerCase().contains("scenario:")) {
            return line.trim().substring(9).trim();
        } else {
            throw new IllegalStateException("Scenario line should start with Scenario: or Scenario Outline but found :" + line);
        }
    }

    private boolean shouldAddJiraId(Map<String, List<String>> cucumberFeatureIssueMap, String scenarioName, String jiraId) {
        List<String> scenarioTags = cucumberFeatureIssueMap.get(scenarioName);

        Optional<String> matchingTag = scenarioTags.stream()
                .filter(tag -> tag.substring(1).equalsIgnoreCase(jiraId))
                .findFirst();

        return matchingTag.isEmpty();
    }
}
