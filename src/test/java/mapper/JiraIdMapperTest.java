package mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.dedalus.xraycucumber.mapper.JiraIdMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JiraIdMapperTest {

    @Test
    public void map_ShouldReturnCorrectMapping() {
        // Arrange
        JsonArray issues = new JsonArray();
        JsonObject issue1 = new JsonObject();
        issue1.addProperty("summary", "Scenario 1");
        issue1.addProperty("key", "JIRA-123");
        issues.add(issue1);

        JsonObject issue2 = new JsonObject();
        issue2.addProperty("summary", "Scenario 2");
        issue2.addProperty("key", "JIRA-124");
        issues.add(issue2);

        JiraIdMapper jiraIdMapper = new JiraIdMapper();

        // Act
        Map<String, String> result = jiraIdMapper.map(issues);

        // Assert
        assertEquals(2, result.size());
        assertEquals("JIRA-123", result.get("Scenario 1"));
        assertEquals("JIRA-124", result.get("Scenario 2"));
    }

    @Test
    void map_ShouldHandleNullOrMissingDataGracefully() {
        // Given
        JiraIdMapper mapper = new JiraIdMapper();
        JsonArray issues = new JsonArray();

        JsonObject issue1 = new JsonObject();
        // "summary" is omitted here
        issue1.addProperty("key", "JIRA-123");
        issues.add(issue1);

        JsonObject issue2 = new JsonObject();
        issue2.addProperty("summary", (String) null);
        issue2.addProperty("key", "JIRA-456");
        issues.add(issue2);

        // When
        Map<String, String> scenarioToJiraIdMap = mapper.map(issues);

        // Then
        assertTrue(scenarioToJiraIdMap.isEmpty(), "Map should be empty due to null/missing summaries");
    }

    @Test
    void map_ShouldReturnEmptyMap_WhenNoIssuesProvided() {
        // Given
        JiraIdMapper mapper = new JiraIdMapper();
        JsonArray issues = new JsonArray(); // Empty issues array

        // When
        Map<String, String> scenarioToJiraIdMap = mapper.map(issues);

        // Then
        assertTrue(scenarioToJiraIdMap.isEmpty(), "Map should be empty as no issues were provided");
    }

    @Test
    void map_ShouldHandleDuplicateScenariosCorrectly() {
        // Given
        JiraIdMapper mapper = new JiraIdMapper();
        JsonArray issues = new JsonArray();

        JsonObject issue1 = new JsonObject();
        issue1.addProperty("summary", "Duplicate Scenario");
        issue1.addProperty("key", "JIRA-123");
        issues.add(issue1);

        JsonObject issue2 = new JsonObject();
        issue2.addProperty("summary", "Duplicate Scenario");
        issue2.addProperty("key", "JIRA-456");
        issues.add(issue2);

        // When
        Map<String, String> scenarioToJiraIdMap = mapper.map(issues);

        // Then
        assertEquals(1, scenarioToJiraIdMap.size(), "Map should contain only one entry for duplicate scenarios");
        assertEquals("JIRA-456", scenarioToJiraIdMap.get("Duplicate Scenario"), "JIRA ID should be of the latter entry for duplicate scenarios");
    }

}

