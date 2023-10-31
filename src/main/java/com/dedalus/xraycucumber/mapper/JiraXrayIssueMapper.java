package com.dedalus.xraycucumber.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
public class JiraXrayIssueMapper {
    private static final String SUMMARY_KEY = "summary";
    private static final String KEY_KEY = "key";

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

    private Optional<String> extractString(JsonObject jsonObject, String key) {
        JsonElement element = jsonObject.get(key);
        return element != null && !element.isJsonNull() ? Optional.of(element.getAsString()) : Optional.empty();
    }
}
