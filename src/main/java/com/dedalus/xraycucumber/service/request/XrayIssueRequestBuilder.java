package com.dedalus.xraycucumber.service.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

public class XrayIssueRequestBuilder {

    public HttpUriRequest build(String issueUrl) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(issueUrl);
        return new HttpGet(uriBuilder.build());
    }

}
