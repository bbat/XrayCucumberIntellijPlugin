package com.dedalus.xraycucumber.service.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpEntityProcessor {
    public JsonArray processResponse(HttpEntity httpEntity, HttpClient httpClient, ServiceParameters serviceParameters) throws IOException, URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        verifyContentType(httpEntity, ContentType.APPLICATION_JSON);
        String responseBody = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
        updateJsonWithIssueDetails(jsonArray, httpClient, serviceParameters);
        return jsonArray;
    }

    private void verifyContentType(HttpEntity httpEntity, ContentType expectedContentType) {
        ContentType contentType = ContentType.getOrDefault(httpEntity);
        if (!contentType.getMimeType().equals(expectedContentType.getMimeType())) {
            throw new IllegalStateException("expected " + expectedContentType.getMimeType() + " but received " + contentType);
        }
    }

    private void updateJsonWithIssueDetails(JsonArray jsonArray, HttpClient httpClient, ServiceParameters serviceParameters)
            throws IOException, URISyntaxException, org.apache.http.auth.AuthenticationException, AuthenticationException {
        for (JsonElement element : jsonArray) {
            JsonObject ticket = element.getAsJsonObject();
            String issueUrl = ticket.get("self").getAsString();
            JsonObject xrayIssueJsonObject = fetchIssueDetails(issueUrl, httpClient, serviceParameters);
            String summary = xrayIssueJsonObject.getAsJsonObject("fields").get("summary").getAsString();
            ticket.addProperty("summary", summary);
        }
    }

    private JsonObject fetchIssueDetails(String issueUrl, HttpClient httpClient, ServiceParameters serviceParameters)
            throws IOException, URISyntaxException, org.apache.http.auth.AuthenticationException, AuthenticationException {
        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder(serviceParameters);
        HttpUriRequest getXrayIssue = xrayIssueRequestBuilder.build(issueUrl, httpClient);
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(httpClient, serviceParameters);
        HttpEntity xrayIssueHttpEntity = httpRequestHandler.executeRequest(getXrayIssue);
        String xrayIssue = EntityUtils.toString(xrayIssueHttpEntity, StandardCharsets.UTF_8);
        return JsonParser.parseString(xrayIssue).getAsJsonObject();
    }
}
