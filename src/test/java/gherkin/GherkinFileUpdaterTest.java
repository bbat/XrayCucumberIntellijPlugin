package gherkin;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
}
