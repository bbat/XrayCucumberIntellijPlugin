package com.dedalus.xraycucumber.test.gherkin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dedalus.xraycucumber.gherkin.GherkinFileUpdater;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

public class GherkinFileUpdaterTest {

    @Test
    void testAddTagsOnScenario_UpdatesFileCorrectly() throws IOException {
        VirtualFile featureFile = mock(VirtualFile.class);
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        Document document = mock(Document.class);

        GherkinFileUpdater updater = new GherkinFileUpdater();

        when(featureFile.getPath()).thenReturn("src/test/resources/updaterTest.feature");
        when(fileDocumentManager.getDocument(featureFile)).thenReturn(document);
        when(document.getLineCount()).thenReturn(3);
        when(document.getLineCount()).thenReturn(3);
        when(document.getText()).thenReturn(
                "Feature: updater test\n"
                        + "Scenario: Scenario1\n"
                        + "Given an action\n");
        when(document.getLineStartOffset(0)).thenReturn(0);
        when(document.getLineEndOffset(0)).thenReturn("Feature: updater test".length());

        when(document.getLineStartOffset(1)).thenReturn("Feature: updater test\n".length());
        when(document.getLineEndOffset(1)).thenReturn("Feature: updater test\nScenario: Scenario1".length());

        when(document.getLineStartOffset(2)).thenReturn("Feature: updater test\nScenario: Scenario1\n".length());
        when(document.getLineEndOffset(2)).thenReturn("Feature: updater test\nScenario: Scenario1\nGiven an action".length());

        Map<String, String> jiraXrayIssueMap = new HashMap<>();
        jiraXrayIssueMap.put("Scenario1", "JIRA-123");

        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put("Scenario1", Collections.singletonList(""));

        //act
        updater.addTagsOnScenario(document, jiraXrayIssueMap, cucumberFeatureIssueMap);

        //assert
        verify(document).setText("Feature: updater test\n@JIRA-123\nScenario: Scenario1\nGiven an action\n");
    }

    @Test
    void testAddTagsOnScenario_JiraIdAlreadyExists() throws IOException {
        VirtualFile featureFile = mock(VirtualFile.class);
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        Document document = mock(Document.class);

        GherkinFileUpdater updater = new GherkinFileUpdater();

        when(featureFile.getPath()).thenReturn("src/test/resources/updaterTest.feature");
        when(fileDocumentManager.getDocument(featureFile)).thenReturn(document);
        when(document.getLineCount()).thenReturn(4);
        when(document.getLineCount()).thenReturn(4);
        when(document.getText()).thenReturn(
                "Feature: updater test\n"
                        + "@Jira-123\n"
                        + "Scenario: Scenario1\n"
                        + "Given an action\n");
        when(document.getLineStartOffset(0)).thenReturn(0);
        when(document.getLineEndOffset(0)).thenReturn("Feature: updater test".length());

        when(document.getLineStartOffset(1)).thenReturn("Feature: updater test\n".length());
        when(document.getLineEndOffset(1)).thenReturn("Feature: updater test\n@Jira-123".length());

        when(document.getLineStartOffset(2)).thenReturn("Feature: updater test\n@Jira-123\n".length());
        when(document.getLineEndOffset(2)).thenReturn("Feature: updater test\n@Jira-123\nScenario: Scenario1".length());

        when(document.getLineStartOffset(3)).thenReturn("Feature: updater test\n@Jira-123\nScenario: Scenario1\n".length());
        when(document.getLineEndOffset(3)).thenReturn("Feature: updater test\n@Jira-123\nScenario: Scenario1\nGiven an action".length());

        Map<String, String> jiraXrayIssueMap = new HashMap<>();
        jiraXrayIssueMap.put("Scenario1", "JIRA-123");

        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put("Scenario1", Collections.singletonList("@Jira-123"));

        //act
        updater.addTagsOnScenario(document, jiraXrayIssueMap, cucumberFeatureIssueMap);

        //assert
        verify(document).setText("Feature: updater test\n@Jira-123\nScenario: Scenario1\nGiven an action\n");
    }

    @Test
    void testAddTagsOnScenario_DocumentIsNull() throws IOException {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();

        // No need for mock configurations as the document is null

        Map<String, String> jiraXrayIssueMap = new HashMap<>();
        jiraXrayIssueMap.put("Scenario1", "JIRA-123");

        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put("Scenario1", Collections.singletonList(""));

        // Act
        Document result = updater.addTagsOnScenario(null, jiraXrayIssueMap, cucumberFeatureIssueMap);

        // Assert
        assertNull(result, "The result should be null when the input document is null");
    }

    @Test
    void testIsJiraIdAbsent_JiraIdIsAbsent() {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();

        String scenarioName = "Scenario1";
        String jiraId = "JIRA-123";
        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put(scenarioName, Collections.singletonList("@SOME-OTHER-ID"));

        // Act
        boolean result = updater.isJiraIdAbsent(cucumberFeatureIssueMap, scenarioName, jiraId);

        // Assert
        assertTrue(result, "Expected true when Jira ID is not present in the scenario tags");
    }

    @Test
    void testIsJiraIdAbsent_JiraIdIsPresent() {
        // Arrange
        GherkinFileUpdater updater = new GherkinFileUpdater();

        String scenarioName = "Scenario1";
        String jiraId = "JIRA-123";
        Map<String, List<String>> cucumberFeatureIssueMap = new HashMap<>();
        cucumberFeatureIssueMap.put(scenarioName, Collections.singletonList("@" + jiraId));

        // Act
        boolean result = updater.isJiraIdAbsent(cucumberFeatureIssueMap, scenarioName, jiraId);

        // Assert
        assertFalse(result, "Expected false when Jira ID is present in the scenario tags");
    }
}
