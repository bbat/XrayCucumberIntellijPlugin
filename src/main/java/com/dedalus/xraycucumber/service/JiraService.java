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

import com.dedalus.xraycucumber.exceptions.JiraException;
import com.dedalus.xraycucumber.service.http.HttpService;
import com.dedalus.xraycucumber.service.request.FeatureUploadRequestBuilder;
import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * JiraService interacts with Jira and Xray by uploading feature files and retrieving scenario details.
 * This service class provides mechanisms to upload feature files to Xray,
 * and to extract and augment relevant scenario details from Jira tickets.
 * <p>
 * Usage example:
 * <pre>
 *     JiraService jiraService = new JiraService(serviceParameters);
 *     JsonArray uploadResponse = jiraService.uploadFeatureToXray(featureFile);
 *     JsonObject scenarioName = jiraService.getScenarioName(issueUrl);
 * </pre>
 */
public class JiraService {

    private static final String XRAY_ISSUE_FIELD_SUMMARY = "summary";
    private static final String XRAY_ISSUE_FIELD_FIELDS = "fields";
    private static final String XRAY_ISSUE_FIELD_SELF = "self";
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
        JsonArray jiraIssueJsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

        return addSummaryToJiraIssueList(jiraIssueJsonArray);
    }

    public JsonObject getJiraIssue(String issueUrl) throws URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder();
        HttpUriRequest request = xrayIssueRequestBuilder.build(issueUrl);

        HttpService httpService = new HttpService(HttpClients.createDefault(), serviceParameters);
        HttpEntity httpEntity = httpService.executeRequest(request);

        String jiraIssueSummary = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        return JsonParser.parseString(jiraIssueSummary).getAsJsonObject();
    }

    private JsonArray addSummaryToJiraIssueList(final JsonArray jiraIssueWithoutSummaryList) throws AuthenticationException, org.apache.http.auth.AuthenticationException, URISyntaxException, IOException {
        JsonArray jiraIssueWithSummaryList = new JsonArray();

        for (JsonElement jiraIssue : jiraIssueWithoutSummaryList) {
            JsonObject jiraIssueWithSummary = addSummaryToJiraIssue(jiraIssue.getAsJsonObject());
            jiraIssueWithSummaryList.add(jiraIssueWithSummary);
        }
        return jiraIssueWithSummaryList;
    }

    private JsonObject addSummaryToJiraIssue(JsonObject jiraIssueWithoutSummary) throws AuthenticationException, org.apache.http.auth.AuthenticationException, URISyntaxException, IOException {
        JsonObject jiraIssueWithSummary = jiraIssueWithoutSummary.deepCopy();

        if (jiraIssueWithoutSummary.has(XRAY_ISSUE_FIELD_SELF) && !jiraIssueWithoutSummary.get(XRAY_ISSUE_FIELD_SELF).isJsonNull()) {
            String issueUrl = jiraIssueWithoutSummary.get(XRAY_ISSUE_FIELD_SELF).getAsString();

            JsonObject jiraIssueJsonObject = getJiraIssue(issueUrl);

            if (jiraIssueJsonObject != null && jiraIssueJsonObject.has(XRAY_ISSUE_FIELD_FIELDS)) {
                JsonObject jiraIssueFields = jiraIssueJsonObject.getAsJsonObject(XRAY_ISSUE_FIELD_FIELDS);

                if (jiraIssueFields.has(XRAY_ISSUE_FIELD_SUMMARY) && !jiraIssueFields.get(XRAY_ISSUE_FIELD_SUMMARY).isJsonNull()) {
                    String summary = jiraIssueFields.get(XRAY_ISSUE_FIELD_SUMMARY).getAsString();
                    jiraIssueWithSummary.addProperty(XRAY_ISSUE_FIELD_SUMMARY, summary);
                } else {
                    throw new JiraException("This issue has no summary");
                }
            } else {
                throw new JiraException("Unexpected error when parsing Issue from Jira");
            }
        } else {
            throw new JiraException("Jira issue has no url");
        }
        return jiraIssueWithSummary;
    }
}
