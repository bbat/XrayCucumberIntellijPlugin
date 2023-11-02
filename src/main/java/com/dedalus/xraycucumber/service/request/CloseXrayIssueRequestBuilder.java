package com.dedalus.xraycucumber.service.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;

public class CloseXrayIssueRequestBuilder {

    private static final String REST_ENDPOINT_CLOSE_ISSUE = "/rest/api/2/issue/";

    private final JiraServiceParameters serviceParameters;

    public CloseXrayIssueRequestBuilder(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public HttpUriRequest build(String xrayIssueId) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(serviceParameters.getUrl() + REST_ENDPOINT_CLOSE_ISSUE + xrayIssueId + "/transitions").addParameter("expand", "transitions.fields");
        HttpPost request = new HttpPost(uriBuilder.build());

        String requestBody = "{\n" + "  \"update\": {\n" + "    \"comment\": [\n" + "      {\n" + "        \"add\": {\n" + "          \"body\": \"Close this issue from IntelliJ.\"\n" + "        }\n" + "      }\n" + "    ]\n" + "  },\n"
                + "  \"transition\": {\n" + "    \"id\": \"51\"\n" + "  }\n" + "}";
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        return request;
    }
}