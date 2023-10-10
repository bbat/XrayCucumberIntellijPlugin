package com.dedalus.xraycucumber.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * The {@code JiraXrayIssueMapper} class is responsible for extracting
 * and mapping JIRA Xray issue information from a JSON array. Specifically,
 * it creates a map linking scenario names to their corresponding JIRA IDs.
 */
public class JiraXrayIssueMapper {
    private static final String SUMMARY_KEY = "summary";
    private static final String KEY_KEY = "key";

    /**
     * Maps scenario names to JIRA IDs based on the provided JSON array.
     *
     * @param issues JSON array containing JIRA issue data.
     * @return A map linking scenario names to JIRA IDs. If a scenario or ID cannot be extracted, they are omitted from the map.
     */
    public Map<String, String> map(JsonArray issues) {
        Map<String, String> scenarioToJiraIdMap = new HashMap<>();

        for (JsonElement issueElement : issues) {
            JsonObject issueObject = issueElement.getAsJsonObject();

            Optional<String> scenarioName = extractString(issueObject, SUMMARY_KEY);
            Optional<String> jiraId = extractString(issueObject, KEY_KEY);

            if (scenarioName.isPresent() && jiraId.isPresent()) {
                scenarioToJiraIdMap.put(scenarioName.get(), jiraId.get());
            }
        }
        return Collections.unmodifiableMap(scenarioToJiraIdMap);
    }

    /**
     * Extracts a string associated with a given key from a JSON object.
     * If the key is not present or associated with a JSON null, an empty Optional is returned.
     *
     * @param jsonObject The JSON object to extract the string from.
     * @param key        The key associated with the string to extract.
     * @return An Optional containing the extracted string or empty if the string cannot be extracted.
     */
    private Optional<String> extractString(JsonObject jsonObject, String key) {
        JsonElement element = jsonObject.get(key);
        return element != null && !element.isJsonNull() ? Optional.of(element.getAsString()) : Optional.empty();
    }
}
