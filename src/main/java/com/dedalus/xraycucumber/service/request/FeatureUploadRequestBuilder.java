package com.dedalus.xraycucumber.service.request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.http.HttpRequestHandler;

public class FeatureUploadRequestBuilder {
    private static final String REST_ENDPOINT_IMPORT_FEATURE = "/rest/raven/1.0/import/feature";

    private final ServiceParameters serviceParameters;

    public FeatureUploadRequestBuilder(final ServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public HttpUriRequest build(Path featureFile, HttpClient httpClient)
            throws AuthenticationException, URISyntaxException, IOException, org.apache.http.auth.AuthenticationException {
        String projectKey = Optional.of(serviceParameters.getProjectKey())
                .orElseThrow(() -> new IllegalArgumentException("projectKey is required to download cucumber tests"));
        URIBuilder uriBuilder = new URIBuilder(serviceParameters.getUrl() + REST_ENDPOINT_IMPORT_FEATURE)
                .addParameter("projectKey", projectKey);
        HttpPost request = new HttpPost(uriBuilder.build());
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(httpClient, serviceParameters);
        httpRequestHandler.addAuthentication(request);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", Files.newInputStream(featureFile), ContentType.APPLICATION_JSON, featureFile.getFileName().toString())
                .build();
        request.setEntity(entity);
        return request;
    }
}
