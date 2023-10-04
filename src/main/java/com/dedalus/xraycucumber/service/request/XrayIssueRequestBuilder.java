package com.dedalus.xraycucumber.service.request;

import java.net.URISyntaxException;

import javax.naming.AuthenticationException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.methods.HttpUriRequest;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.http.HttpRequestHandler;

public class XrayIssueRequestBuilder {
    private final ServiceParameters serviceParameters;

    public XrayIssueRequestBuilder(final ServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public HttpUriRequest build(String issueUrl, HttpClient httpClient)
            throws AuthenticationException, URISyntaxException, org.apache.http.auth.AuthenticationException {
        URIBuilder uriBuilder = new URIBuilder(issueUrl);
        HttpGet request = new HttpGet(uriBuilder.build());
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(httpClient, serviceParameters);
        httpRequestHandler.addAuthentication(request);
        return request;
    }

}
