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

import com.dedalus.xraycucumber.service.http.HttpService;
import com.dedalus.xraycucumber.service.request.FeatureUploadRequestBuilder;
import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * JiraService interacts with Jira and Xray by uploading feature files and retrieving scenario details.
 *
 * This service class provides mechanisms to upload feature files to Xray,
 * and to extract and augment relevant scenario details from Jira tickets.
 *
 * Usage example:
 * <pre>
 *     JiraService jiraService = new JiraService(serviceParameters);
 *     JsonArray uploadResponse = jiraService.uploadFeatureToXray(featureFile);
 *     JsonObject scenarioName = jiraService.getScenarioName(issueUrl);
 * </pre>
 */
public class JiraService {

    private static final Logger LOG = Logger.getInstance(JiraService.class);
    private static final String XRAY_ISSUE_FIELD_SUMMARY = "summary";
    private static final String XRAY_ISSUE_FIELD_FIELDS = "fields";
    private static final String XRAY_ISSUE_FIELD_SELF = "self";
    private final JiraServiceParameters serviceParameters;

    /**
     * Constructs a JiraService with specified service parameters.
     *
     * @param serviceParameters the parameters necessary for service operation such as authentication info, etc.
     */
    public JiraService(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    /**
     * Uploads a feature file to Xray and retrieves the corresponding Jira response.
     *
     * @param featureFile the feature file to be uploaded to Xray.
     * @return a JsonArray containing the response from Jira after feature file upload.
     * @throws URISyntaxException if the URL is malformed.
     * @throws IOException if there's an issue with I/O operations during HTTP request execution or response reading.
     * @throws AuthenticationException if there's an issue with authentication during HTTP request execution.
     * @throws org.apache.http.auth.AuthenticationException if there's an authentication-related issue.
     */
    public JsonArray uploadFeatureToXray(VirtualFile featureFile) throws URISyntaxException, IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        FeatureUploadRequestBuilder featureUploadRequestBuilder = new FeatureUploadRequestBuilder(serviceParameters);
        HttpUriRequest request = featureUploadRequestBuilder.build(Paths.get(featureFile.getPath()));

        HttpService httpService = new HttpService(HttpClients.createDefault(), serviceParameters);
        HttpEntity httpEntity = httpService.executeRequest(request);

        String responseBody = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JsonArray originalResponseBody = JsonParser.parseString(responseBody).getAsJsonArray();

        return addScenarioName(originalResponseBody);
    }

    /**
     * Retrieves the scenario name from Jira based on the issue URL.
     *
     * @param issueUrl the URL of the Jira issue.
     * @return a JsonObject containing the scenario name extracted from the Jira issue.
     * @throws URISyntaxException if the URL is malformed.
     * @throws IOException if there's an issue with I/O operations during HTTP request execution or response reading.
     * @throws AuthenticationException if there's an issue with authentication during HTTP request execution.
     * @throws org.apache.http.auth.AuthenticationException if there's an authentication-related issue.
     */
    public JsonObject getScenarioName(String issueUrl) throws URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException, IOException {
        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder();
        HttpUriRequest request = xrayIssueRequestBuilder.build(issueUrl);

        HttpService httpService = new HttpService(HttpClients.createDefault(), serviceParameters);
        HttpEntity httpEntity = httpService.executeRequest(request);

        String scenarioName = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        return JsonParser.parseString(scenarioName).getAsJsonObject();
    }

    /**
     * Enhances the Jira upload response with additional scenario names.
     * <p>
     * Iterates over the elements in the Jira upload response, processes each ticket, and adds scenario details.
     *
     * @param jiraUploadResponse the original response from Jira after feature file upload.
     * @return a JsonArray containing enhanced ticket details with scenario names.
     * @throws AuthenticationException if there's an authentication issue during HTTP request execution.
     * @throws org.apache.http.auth.AuthenticationException if there's an authentication-related issue.
     * @throws URISyntaxException if the URL is malformed.
     * @throws IOException if there's an issue with I/O operations during HTTP request execution or response reading.
     */
    private JsonArray addScenarioName(final JsonArray jiraUploadResponse) throws AuthenticationException, org.apache.http.auth.AuthenticationException, URISyntaxException, IOException {
        for (JsonElement element : jiraUploadResponse) {
            processTicket(element.getAsJsonObject());
        }
        return jiraUploadResponse;
    }

    /**
     * Processes a single ticket JsonObject, retrieving scenario details and enhancing the ticket with them.
     * <p>
     * Extracts the 'self' URL from the ticket, retrieves the scenario name from Jira, and adds it back to the original ticket data.
     *
     * @param ticket a JsonObject representing a ticket from the Jira response.
     * @throws AuthenticationException if there's an authentication issue during HTTP request execution.
     * @throws org.apache.http.auth.AuthenticationException if there's an authentication-related issue.
     * @throws URISyntaxException if the URL is malformed.
     * @throws IOException if there's an issue with I/O operations during HTTP request execution or response reading.
     */
    private void processTicket(JsonObject ticket) throws AuthenticationException, org.apache.http.auth.AuthenticationException, URISyntaxException, IOException {
        if (ticket.has(XRAY_ISSUE_FIELD_SELF) && !ticket.get(XRAY_ISSUE_FIELD_SELF).isJsonNull()) {
            String issueUrl = ticket.get(XRAY_ISSUE_FIELD_SELF).getAsString();
            JsonObject xrayIssueJsonObject;
            xrayIssueJsonObject = getScenarioName(issueUrl);
            addSummaryToTicket(ticket, xrayIssueJsonObject);
        } else {
            LOG.warn("'self' key is missing or null in object: " + ticket);
        }
    }

    /**
     * Enhances a ticket with summary details from an Xray issue.
     * <p>
     * Extracts the 'summary' from the provided Xray issue JsonObject and adds it to the ticket JsonObject.
     *
     * @param ticket a JsonObject to which the summary details will be added.
     * @param xrayIssueJsonObject a JsonObject containing issue details from Xray.
     */
    private void addSummaryToTicket(JsonObject ticket, JsonObject xrayIssueJsonObject) {
        if (xrayIssueJsonObject != null && xrayIssueJsonObject.has(XRAY_ISSUE_FIELD_FIELDS)) {
            JsonObject fields = xrayIssueJsonObject.getAsJsonObject(XRAY_ISSUE_FIELD_FIELDS);

            if (fields.has(XRAY_ISSUE_FIELD_SUMMARY) && !fields.get(XRAY_ISSUE_FIELD_SUMMARY).isJsonNull()) {
                String summary = fields.get(XRAY_ISSUE_FIELD_SUMMARY).getAsString();
                ticket.addProperty(XRAY_ISSUE_FIELD_SUMMARY, summary);
            } else {
                LOG.warn("'summary' key is missing or null in object: " + fields);
            }
        } else {
            LOG.warn("fetchIssueDetails returned unexpected object: " + xrayIssueJsonObject);
        }
    }
}
