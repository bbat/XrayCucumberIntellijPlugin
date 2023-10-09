package gherkin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dedalus.xraycucumber.gherkin.GherkinFileParser;
import com.intellij.openapi.vfs.VirtualFile;

public class GherkinFileParserTest {

    @Test
    public void testGetScenariosAndTagsNominal() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "src/test/resources/dummy.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Act
        Map<String, List<String>> result = parser.getScenariosAndTags(featureFile);

        // Assert
        assertEquals(2, result.size(), "Expected two scenarios parsed");
        assertEquals(List.of("@tag1"), result.get("Test scenario 1"), "Tags do not match for scenario 1");
        assertEquals(List.of("@tag2"), result.get("Test scenario 2"), "Tags do not match for scenario 2");
    }

    @Test
    public void testGetScenariosAndTags_EmptyFile() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "src/test/resources/empty.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Assume empty.feature is an empty file.

        // Act
        Map<String, List<String>> result = parser.getScenariosAndTags(featureFile);

        // Assert
        assertTrue(result.isEmpty(), "Expected no scenarios to be parsed from an empty file");
    }

    @Test
    public void testGetScenariosAndTags_FileNotFound() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "path/to/nonexistent.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> parser.getScenariosAndTags(featureFile),
                "Expected RuntimeException to be thrown for non-existent feature file");
    }

    @Test
    public void testGetScenariosAndTags_MalformedFeatureFile() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "src/test/resources/malformed.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> parser.getScenariosAndTags(featureFile),
                "Expected IllegalStateException to be thrown for malformed feature file");
    }

    @Test
    public void testGetScenariosAndTags_ScenarioLessFeatureFile() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "src/test/resources/scenarioLess.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> parser.getScenariosAndTags(featureFile),
                "Expected IllegalStateException to be thrown for scenario less feature file");
    }

    @Test
    public void testGetScenariosAndTags_TagLessScenario() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String dummyFeaturePath = "src/test/resources/tagLess.feature";
        when(featureFile.getPath()).thenReturn(dummyFeaturePath);

        // Act
        Map<String, List<String>> result = parser.getScenariosAndTags(featureFile);

        // Assert
        assertTrue(result.containsKey("Test scenario 1"),
                "Expected result to contain key 'Test scenario 1'");
        assertTrue(result.containsKey("Test scenario 2"),
                        "Expected result to contain key 'Test scenario 2'");

        assertTrue(result.get("Test scenario 1").isEmpty(),
                "Expected tag list for scenario 'Test scenario 1' to be empty");
        assertTrue(result.get("Test scenario 2").isEmpty(),
                "Expected tag list for scenario 'Test scenario 1' to be empty");
    }

    @Test
    public void testGetScenariosAndTags_DuplicateScenarios() {
        // Arrange
        GherkinFileParser parser = new GherkinFileParser();
        VirtualFile featureFile = mock(VirtualFile.class);

        String featureFilePath = "src/test/resources/featureWithDuplicateScenarios.feature";
        when(featureFile.getPath()).thenReturn(featureFilePath);

        // Act&Assert
        assertThrows(IllegalStateException.class, () -> parser.getScenariosAndTags(featureFile),
                "Expected IllegalStateException to be thrown for scenario less feature file");
    }
}
