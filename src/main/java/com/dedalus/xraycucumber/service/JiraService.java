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
import com.dedalus.xraycucumber.service.request.CloseXrayIssueRequestBuilder;
import com.dedalus.xraycucumber.service.request.FeatureUploadRequestBuilder;
import com.dedalus.xraycucumber.service.request.JiraIssueRequestBuilder;
import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.vfs.VirtualFile;
public class JiraService {

    private static final String XRAY_ISSUE_FIELD_SUMMARY = "summary";
    private static final String XRAY_ISSUE_FIELD_STATUS = "status";
    private static final String XRAY_ISSUE_FIELD_STATUS_NAME = "name";
    private static final String XRAY_ISSUE_FIELD_FIELDS = "fields";
    private static final String XRAY_ISSUE_FIELD_SELF = "self";
    private final JiraServiceParameters serviceParameters;
    private final Credentials credentials;
    private final String token;

    public JiraService(final JiraServiceParameters serviceParameters, Credentials credentials) {
        this.serviceParameters = serviceParameters;
        this.credentials = credentials;
        this.token = null;
    }

    public JiraService(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
        this.credentials = null;
        this.token = serviceParameters.getBearerToken();
    }

    public void closeXrayIssue(String xrayIssueId) throws URISyntaxException, IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        CloseXrayIssueRequestBuilder closeXrayIssueRequestBuilder = new CloseXrayIssueRequestBuilder(serviceParameters);
        HttpUriRequest request = closeXrayIssueRequestBuilder.build(xrayIssueId);

        HttpService httpService = new HttpService(HttpClients.createDefault());
        executeRequest(httpService, request);
    }

    public JsonArray uploadFeatureToXray(VirtualFile featureFile) throws URISyntaxException, IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        FeatureUploadRequestBuilder featureUploadRequestBuilder = new FeatureUploadRequestBuilder(serviceParameters);
        HttpUriRequest request = featureUploadRequestBuilder.build(Paths.get(featureFile.getPath()));

        HttpService httpService = new HttpService(HttpClients.createDefault());
        HttpEntity httpEntity = executeRequest(httpService, request);

        String responseBody = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JsonArray jiraIssueJsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

        return addSummaryToJiraIssueList(jiraIssueJsonArray);
    }

    public JsonObject getJiraIssueFromUrl(String issueUrl) throws URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder();
        HttpUriRequest request = xrayIssueRequestBuilder.build(issueUrl);

        HttpService httpService = new HttpService(HttpClients.createDefault());
        HttpEntity httpEntity = executeRequest(httpService, request);

        String jiraIssueSummary = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        return JsonParser.parseString(jiraIssueSummary).getAsJsonObject();
    }

    private HttpEntity executeRequest(HttpService httpService, HttpUriRequest request) throws AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        if(token == null && credentials==null) {
            throw new IllegalStateException("Token and Credentials are null");
        }
        if(token==null){
            return httpService.executeRequest(request, credentials);
        } else {
            return httpService.executeRequest(request, token);
        }
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

            JsonObject jiraIssueJsonObject = getJiraIssueFromUrl(issueUrl);

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

    public String getXrayIssueStatus(final String xrayIssue) throws URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        JiraIssueRequestBuilder jiraIssueRequestBuilder = new JiraIssueRequestBuilder(serviceParameters);
        HttpUriRequest request = jiraIssueRequestBuilder.build(xrayIssue);

        HttpService httpService = new HttpService(HttpClients.createDefault());
        HttpEntity httpEntity = executeRequest(httpService, request);

        String response = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JsonObject jiraIssueJsonObject = JsonParser.parseString(response).getAsJsonObject();

        if (jiraIssueJsonObject != null && jiraIssueJsonObject.has(XRAY_ISSUE_FIELD_FIELDS)) {
            JsonObject jiraIssueFields = jiraIssueJsonObject.getAsJsonObject(XRAY_ISSUE_FIELD_FIELDS);

            if (jiraIssueFields.has(XRAY_ISSUE_FIELD_STATUS) && !jiraIssueFields.get(XRAY_ISSUE_FIELD_STATUS).isJsonNull()) {
                return jiraIssueFields.get(XRAY_ISSUE_FIELD_STATUS).getAsJsonObject().get(XRAY_ISSUE_FIELD_STATUS_NAME).getAsString();
            } else {
                throw new JiraException("This issue has no summary");
            }
        } else {
            throw new JiraException("Unexpected error when parsing Issue from Jira");
        }
    }
}
