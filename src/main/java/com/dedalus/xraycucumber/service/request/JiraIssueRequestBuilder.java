package com.dedalus.xraycucumber.service.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;

public class JiraIssueRequestBuilder {
    private static final String REST_ENDPOINT = "/rest/api/2/issue/";
    private final JiraServiceParameters serviceParameters;

    public JiraIssueRequestBuilder(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public HttpUriRequest build(String issueId) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(serviceParameters.getUrl() + REST_ENDPOINT + issueId);
        return new HttpGet(uriBuilder.build());
    }

}
