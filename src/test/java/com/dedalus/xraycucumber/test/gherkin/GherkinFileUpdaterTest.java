package com.dedalus.xraycucumber.test.gherkin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dedalus.xraycucumber.gherkin.GherkinFileUpdater;

public class GherkinFileUpdaterTest {

    @Test void testAddTagsOnScenario_DocumentIsNull() {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();
        // No need for mock configurations as the document is null

        Map<String, String> jiraXrayIssueMap = new HashMap<>();
        jiraXrayIssueMap.put("Scenario1", "JIRA-123");

        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put("Scenario1", Collections.singletonList(""));

        // Act & Assert
        try {
            // Act
            updater.addXrayIssueIdTagsOnScenario(null, jiraXrayIssueMap, cucumberFeatureIssueMap);

            // Si aucune exception n'est levée, le test échouera
            Assertions.fail("Expected IllegalArgumentException to be thrown for null feature file");
        } catch (IllegalArgumentException e) {
            // Assert
            // Vérifiez que l'exception IllegalArgumentException est bien levée
            Assertions.assertNotNull(e);
        }
    }

    @Test void testIsJiraIdAbsent_JiraIdIsAbsent() {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();

        String scenarioName = "Scenario1";
        String jiraId = "JIRA-123";
        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put(scenarioName, Collections.singletonList("@SOME-OTHER-ID"));

        // Act
        boolean result = updater.isJiraIdTagAbsentOnScenario(cucumberFeatureIssueMap, scenarioName, jiraId);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test void testIsJiraIdAbsent_JiraIdIsPresent() {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();

        String scenarioName = "Scenario1";
        String jiraId = "JIRA-123";
        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put(scenarioName, Collections.singletonList("@" + jiraId));

        // Act
        boolean result = updater.isJiraIdTagAbsentOnScenario(cucumberFeatureIssueMap, scenarioName, jiraId);

        // Assert
        Assertions.assertFalse(result);
    }
}
