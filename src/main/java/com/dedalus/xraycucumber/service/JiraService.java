package com.dedalus.xraycucumber.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.service.http.HttpService;
import com.dedalus.xraycucumber.service.request.FeatureUploadRequestBuilder;
import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.vfs.VirtualFile;

public class JiraService {
    private final JiraServiceParameters serviceParameters;

    public JiraService(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public JsonArray uploadFeatureToXray(VirtualFile featureFile) throws URISyntaxException, IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {

        FeatureUploadRequestBuilder featureUploadRequestBuilder = new FeatureUploadRequestBuilder(serviceParameters);
        HttpUriRequest request = featureUploadRequestBuilder.build(Paths.get(featureFile.getPath()));

        HttpService httpService = new HttpService(HttpClients.createDefault(), serviceParameters);
        HttpEntity httpEntity = httpService.executeRequest(request);

        String responseBody = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JsonArray originalResponseBody = JsonParser.parseString(responseBody).getAsJsonArray();

        return addScenarioName(originalResponseBody);
    }

    public JsonObject getScenarioName(String issueUrl) throws URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder();
        HttpUriRequest request = xrayIssueRequestBuilder.build(issueUrl);

        HttpService httpService = new HttpService(HttpClients.createDefault(), serviceParameters);
        HttpEntity httpEntity = httpService.executeRequest(request);

        String scenarioName = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        return JsonParser.parseString(scenarioName).getAsJsonObject();
    }

    private JsonArray addScenarioName(final JsonArray jiraUploadResponse) throws AuthenticationException, org.apache.http.auth.AuthenticationException, URISyntaxException, IOException {
        for (JsonElement element : jiraUploadResponse) {
            JsonObject ticket = element.getAsJsonObject();

            if (ticket.has("self") && !ticket.get("self").isJsonNull()) {
                String issueUrl = ticket.get("self").getAsString();

                JsonObject xrayIssueJsonObject = getScenarioName(issueUrl);
                if (xrayIssueJsonObject != null && xrayIssueJsonObject.has("fields")) {
                    JsonObject fields = xrayIssueJsonObject.getAsJsonObject("fields");

                    if (fields.has("summary") && !fields.get("summary").isJsonNull()) {
                        String summary = fields.get("summary").getAsString();
                        ticket.addProperty("summary", summary);
                    } else {
                        System.err.println("Warning: 'summary' key is missing or null in object: " + fields);
                    }
                } else {
                    System.err.println("Warning: fetchIssueDetails returned unexpected object: " + xrayIssueJsonObject);
                }
            } else {
                System.err.println("Warning: 'self' key is missing or null in object: " + ticket);
            }
        }
        return jiraUploadResponse;
    }
}
