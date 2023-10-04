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

import com.dedalus.xraycucumber.mapper.JiraIdMapper;
import com.google.gson.JsonArray;
import com.intellij.openapi.vfs.VirtualFile;

public class GherkinFileUpdater {

    public void update(VirtualFile featureFile, JsonArray issues) throws IOException {
        saveBeforeUpdate(featureFile);

        String updatedFeatureContent = addTagsOnScenario(
                Files.readAllLines(Paths.get(featureFile.getPath())),
                new JiraIdMapper().map(issues),
                new GherkinFileParser().getScenariosAndTags(featureFile));

        Files.write(Paths.get(featureFile.getPath()), updatedFeatureContent.getBytes());

    }

    private String addTagsOnScenario(final List<String> featureLines, final Map<String, String> jiraIdMap, final Map<String, List<String>> scenariosAndTags) {
        StringBuilder updatedFeatureContent = new StringBuilder();

        for (String line : featureLines) {
            if (line.trim().toLowerCase().startsWith("scenario:") || line.trim().toLowerCase().startsWith("scenario outline:")) {
                String scenarioName = extractScenarioName(line);

                String jiraId = jiraIdMap.get(scenarioName);
                if (shouldAddJiraId(scenariosAndTags, scenarioName, jiraId)) {
                    updatedFeatureContent.append("@").append(jiraId).append("\n");
                }
            }
            updatedFeatureContent.append(line).append("\n");
        }
        return updatedFeatureContent.toString();
    }

    private String extractScenarioName(String line) {
        if(line.toLowerCase().contains("outline:")) {
            return line.trim().substring(17).trim();
        } else if(line.toLowerCase().contains("scenario:")) {
            return line.trim().substring(9).trim();
        } else {
            throw new IllegalStateException("Scenario line should start with Scenario: or Scenario Outline but found :" + line);
        }
    }

    private boolean shouldAddJiraId(Map<String, List<String>> scenariosAndTags, String scenarioName, String jiraId) {
        return jiraId != null && (scenariosAndTags.get(scenarioName) == null || !scenariosAndTags.get(scenarioName).contains("@" + jiraId));
    }

    private static void saveBeforeUpdate(final VirtualFile featureFile) throws IOException {
        try{
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
}
