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
import com.intellij.openapi.vfs.VirtualFile;

/**
 * The {@code GherkinFileUpdater} class provides utility methods for managing
 * and updating Gherkin feature files, specifically aimed at adding tags to
 * scenario definitions based on provided mappings, and facilitating backup
 * procedures prior to updates.
 *
 * <p>The class relies heavily on regular expressions to identify scenario
 * definitions within a feature file, and performs string manipulation and
 * document updates as necessary to incorporate new tags.</p>
 */
public class GherkinFileUpdater {

    private static final Pattern SCENARIO_NAME_PATTERN = Pattern.compile("(Scenario Outline|Scenario):(.*)", Pattern.CASE_INSENSITIVE);
    private static final String ERROR_MSG_PATH_RETRIEVAL = "Can't get path from ";

    /**
     * Analyzes a provided document and adds tags to scenarios in the Gherkin feature file.
     * The tags, derived from a map of JIRA Xray issues, are added based on the scenario name.
     *
     * @param document              The document representing the feature file.
     * @param jiraXrayIssueMap      A map linking scenario names to JIRA Xray issue IDs.
     * @param cucumberFeatureIssueMap A map linking scenario names to lists of associated issue tags.
     * @return                      The modified document with added tags.
     * @throws IOException          If an I/O error occurs during document manipulation.
     *
     */
    public Document addTagsOnScenario(Document document, final Map<String, String> jiraXrayIssueMap, final Map<String, List<String>> cucumberFeatureIssueMap) {
        if (document != null) {
            StringBuilder newContent = new StringBuilder();

            for (int i = 0; i < document.getLineCount(); i++) {
                int startOffset = document.getLineStartOffset(i);
                int endOffset = document.getLineEndOffset(i);

                String line = document.getText().substring(startOffset, endOffset);
                Matcher matcher = SCENARIO_NAME_PATTERN.matcher(line.trim());

                if (matcher.find()) {
                    String scenarioName = matcher.group(2).trim();
                    String jiraXrayIssueId = jiraXrayIssueMap.get(scenarioName);

                    if (isJiraIdAbsent(cucumberFeatureIssueMap, scenarioName, jiraXrayIssueId)) {
                        newContent.append("@").append(jiraXrayIssueId).append("\n");
                    }
                }
                newContent.append(line).append("\n");
            }

            document.setText(newContent.toString());
        }
        return document;
    }

    /**
     * Creates a backup of the original feature file before it undergoes any updates.
     * The backup filename is generated based on the original filename and a timestamp.
     *
     * @param featureFile           The virtual file representing the original feature file.
     * @throws IOException          If an I/O error occurs during the file copy process.
     * @throws InvalidPathException If the path of the provided virtual file cannot be resolved.
     *
     * <p>Backups are stored in the same directory as the original file with a distinct, timestamped filename.</p>
     */
    public void saveBeforeUpdate(final VirtualFile featureFile) throws IOException {
        try {
            Path originalPath = Paths.get(featureFile.getPath());
            Path backupPath = getBackupPath(originalPath);

            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (InvalidPathException e) {
            throw new InvalidPathException(featureFile.getPath(), ERROR_MSG_PATH_RETRIEVAL + featureFile.getPath());
        }
    }

    /**
     * Determines whether a specific JIRA ID is absent in the tags associated with a scenario.
     *
     * @param cucumberFeatureIssueMap A map linking scenario names to lists of associated issue tags.
     * @param scenarioName            The name of the scenario being checked.
     * @param jiraId                  The JIRA ID to check for within the scenario's tags.
     * @return                        {@code true} if the JIRA ID is not found among the scenario's tags, {@code false} otherwise.
     *
     * <p>Only checks the absence of JIRA ID. No addition or removal of tags is performed by this method.</p>
     */
    public boolean isJiraIdAbsent(Map<String, List<String>> cucumberFeatureIssueMap, String scenarioName, String jiraId) {
        //if jiraId is already present as a scenario tag then no need to add it again
        List<String> scenarioTags = cucumberFeatureIssueMap.getOrDefault(scenarioName, Collections.emptyList());

        return scenarioTags.stream()
                .filter(tag -> !tag.isEmpty())
                .noneMatch(tag -> tag.substring(1).equalsIgnoreCase(jiraId));
    }

    /**
     * Constructs and returns the path for a backup file derived from the original file path.
     * The method generates a timestamped backup filename to avoid conflicts and
     * ensures easy identification of backup instances.
     *
     * @param originalPath           The path of the original feature file.
     * @return                       The path for the backup file.
     * @throws InvalidPathException  If the parent directory path of the original file cannot be resolved.
     *
     * <p>Backup filenames are formed by appending a timestamp to the original filename.
     * If the parent path cannot be determined, an InvalidPathException is thrown with relevant details.</p>
     */
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
