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

            JsonElement summaryElement = issueObject.get("summary");
            JsonElement keyElement = issueObject.get("key");

            if (summaryElement != null && !summaryElement.isJsonNull()
                && keyElement != null && !keyElement.isJsonNull()) {

                String scenarioName = summaryElement.getAsString();
                String jiraId = keyElement.getAsString();
                scenarioToJiraIdMap.put(scenarioName, jiraId);
            }
        }
        return scenarioToJiraIdMap;
    }

}
