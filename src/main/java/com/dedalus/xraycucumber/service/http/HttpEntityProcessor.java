package com.dedalus.xraycucumber.service.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.dedalus.xraycucumber.service.request.XrayIssueRequestBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpEntityProcessor {

    private final HttpService httpService;

    public HttpEntityProcessor(HttpService httpService) {
        this.httpService = httpService;
    }

    public JsonArray processResponse(HttpEntity httpEntity) throws IOException, URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        verifyContentType(httpEntity, ContentType.APPLICATION_JSON);

        String responseBody = readResponseBody(httpEntity);
        JsonArray jsonArray = parseResponse(responseBody);
        updateJsonWithIssueDetails(jsonArray);

        return jsonArray;
    }

    private String readResponseBody(HttpEntity httpEntity) throws IOException {
        return EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
    }

    private JsonArray parseResponse(String responseBody) {
        return JsonParser.parseString(responseBody).getAsJsonArray();
    }

    private void verifyContentType(HttpEntity httpEntity, ContentType expectedContentType) {
        ContentType contentType = ContentType.getOrDefault(httpEntity);
        if (!contentType.getMimeType().equals(expectedContentType.getMimeType())) {
            throw new IllegalStateException("expected " + expectedContentType.getMimeType() + " but received " + contentType);
        }
    }

    private void updateJsonWithIssueDetails(JsonArray jsonArray)
            throws IOException, URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        for (JsonElement element : jsonArray) {
            JsonObject ticket = element.getAsJsonObject();

            if (ticket.has("self") && !ticket.get("self").isJsonNull()) {
                String issueUrl = ticket.get("self").getAsString();

                JsonObject xrayIssueJsonObject = fetchIssueDetails(issueUrl);
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
    }

    private JsonObject fetchIssueDetails(String issueUrl)
            throws IOException, URISyntaxException, AuthenticationException, org.apache.http.auth.AuthenticationException {

        XrayIssueRequestBuilder xrayIssueRequestBuilder = new XrayIssueRequestBuilder();
        HttpUriRequest getXrayIssue = xrayIssueRequestBuilder.build(issueUrl);

        HttpEntity xrayIssueHttpEntity = httpService.executeRequest(getXrayIssue);
        String xrayIssue = EntityUtils.toString(xrayIssueHttpEntity, StandardCharsets.UTF_8);

        return JsonParser.parseString(xrayIssue).getAsJsonObject();
    }
}
