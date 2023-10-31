package com.dedalus.xraycucumber.gherkin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.vfs.VirtualFile;

public class GherkinFileUpdater {

    private static final Pattern SCENARIO_NAME_PATTERN = Pattern.compile("(Scenario Outline|Scenario):(.*)", Pattern.CASE_INSENSITIVE);
    private static final String ERROR_MSG_PATH_RETRIEVAL = "Can't get path from ";

    /**
     * Adds Xray Issue id as tags to scenarios in the Gherkin feature file.
     * The tags, derived from a map of JIRA Xray issues, are added based on the scenario name.
     *
     * @param featureFile              The document representing the feature file.
     * @param jiraXrayIssueMap      A map linking scenario names to JIRA Xray issue IDs.
     * @param cucumberFeatureIssueMap A map linking scenario names to lists of associated issue tags.
     * @return A copy of the featureFile with added tags.
     *
     */
    public Document addXrayIssueIdTagsOnScenario(Document featureFile, final Map<String, String> jiraXrayIssueMap, final Map<String, List<String>> cucumberFeatureIssueMap) {
        if (featureFile == null) {
            throw new IllegalArgumentException("Feature file can't be null");
        }

        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(featureFile);
        Document newDocument = editor.getDocument();

        String originalContent = featureFile.getText();
        StringBuilder newContent = new StringBuilder();

        for (int i = 0; i < featureFile.getLineCount(); i++) {
            int startOffset = featureFile.getLineStartOffset(i);
            int endOffset = featureFile.getLineEndOffset(i);

            String line = originalContent.substring(startOffset, endOffset);
            Matcher matcher = SCENARIO_NAME_PATTERN.matcher(line.trim());

            if (matcher.find()) {
                String scenarioName = matcher.group(2).trim();
                String jiraXrayIssueId = jiraXrayIssueMap.get(scenarioName);

                if (isJiraIdTagAbsentOnScenario(cucumberFeatureIssueMap, scenarioName, jiraXrayIssueId)) {
                    newContent.append("@").append(jiraXrayIssueId).append("\n");
                }
            }
            newContent.append(line).append("\n");
        }

        if (newContent.length() > 0) {
            newContent.setLength(newContent.length() - 1);
        }

        newDocument.setText(newContent.toString());
        editorFactory.releaseEditor(editor);

        return newDocument;
    }

    public void saveBeforeUpdate(final VirtualFile featureFile) throws IOException {
        try {
            Path originalPath = Paths.get(featureFile.getPath());
            Path backupPath = getBackupPath(originalPath);

            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (InvalidPathException e) {
            throw new InvalidPathException(featureFile.getPath(), ERROR_MSG_PATH_RETRIEVAL + featureFile.getPath());
        }
    }

    public boolean isJiraIdTagAbsentOnScenario(Map<String, List<String>> cucumberFeatureIssueMap, String scenarioName, String jiraId) {
        //if jiraId is already present as a scenario tag then no need to add it again
        List<String> scenarioTags = cucumberFeatureIssueMap.getOrDefault(scenarioName, Collections.emptyList());

        return scenarioTags.stream().filter(tag -> !tag.isEmpty()).noneMatch(tag -> tag.substring(1).equalsIgnoreCase(jiraId));
    }

    private Path getBackupPath(Path originalPath) {
        String originalNameWithoutExtension = originalPath.getFileName().toString().replace(".feature", "");
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String backupFileName = originalNameWithoutExtension + "_Backup-" + timeStamp + ".feature";

        Path parentPath = originalPath.getParent();
        if (parentPath != null) {
            return parentPath.resolve(backupFileName);
        } else {
            throw new InvalidPathException(backupFileName, "No parent path found for " + originalPath.getFileName().toString());
        }
    }
}
