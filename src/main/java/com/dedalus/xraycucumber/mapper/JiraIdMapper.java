package com.dedalus.xraycucumber.mapper;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JiraIdMapper {
    public Map<String, String> map(JsonArray issues) {
        Map<String, String> scenarioToJiraIdMap = new HashMap<>();

        for (JsonElement issueElement : issues) {
            JsonObject issueObject = issueElement.getAsJsonObject();

            String scenarioName = issueObject.get("summary").getAsString();
            String jiraId = issueObject.get("key").getAsString();

            scenarioToJiraIdMap.put(scenarioName, jiraId);
        }

        return scenarioToJiraIdMap;
    }

}
