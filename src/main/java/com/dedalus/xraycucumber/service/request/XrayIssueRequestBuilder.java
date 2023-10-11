package com.dedalus.xraycucumber.service.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

/**
 * Responsible for building HTTP requests to retrieve details of a specific Xray issue from Jira.
 */
public class XrayIssueRequestBuilder {

    /**
     * Constructs and returns an HTTP GET request targeted at the provided issue URL.
     * The method is designed to facilitate the fetching of details related to a specific Xray issue in Jira.
     *
     * @param issueUrl the URL of the Xray issue to retrieve.
     * @return a configured HttpUriRequest ready to fetch the specified Xray issue.
     * @throws URISyntaxException if the provided issue URL is not a valid URI.
     */
    public HttpUriRequest build(String issueUrl) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(issueUrl);
        return new HttpGet(uriBuilder.build());
    }

}
