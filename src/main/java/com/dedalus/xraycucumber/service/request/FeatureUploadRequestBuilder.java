package com.dedalus.xraycucumber.service.request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;

/**
 * Responsible for building HTTP requests intended for uploading feature files to Jira.
 */
public class FeatureUploadRequestBuilder {

    private static final String REST_ENDPOINT_IMPORT_FEATURE = "/rest/raven/1.0/import/feature";

    private final JiraServiceParameters serviceParameters;

    /**
     * Constructs a new instance with the provided Jira service parameters.
     *
     * @param serviceParameters the parameters necessary for Jira service interaction.
     */
    public FeatureUploadRequestBuilder(final JiraServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    /**
     * Builds an HTTP request meant to upload a feature file to Jira's Xray. The project key is extracted from the
     * service parameters and is used to identify the Jira project to which the feature file should be uploaded.
     * The constructed request is a POST request with the feature file attached as a binary body.
     *
     * @param featureFile the path of the feature file to be uploaded.
     * @return an HttpUriRequest configured to upload the provided feature file to Jira.
     * @throws URISyntaxException if the constructed URI is incorrect.
     * @throws IOException if an I/O error occurs when opening the feature file.
     * @throws IllegalArgumentException if the project key is missing from the service parameters.
     */
    public HttpUriRequest build(Path featureFile) throws URISyntaxException, IOException {
        String projectKey = Optional.of(serviceParameters.getProjectKey()).orElseThrow(() -> new IllegalArgumentException("projectKey is required to download cucumber tests"));
        URIBuilder uriBuilder = new URIBuilder(serviceParameters.getUrl() + REST_ENDPOINT_IMPORT_FEATURE).addParameter("projectKey", projectKey);
        HttpPost request = new HttpPost(uriBuilder.build());

        HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("file", Files.newInputStream(featureFile), ContentType.APPLICATION_JSON, featureFile.getFileName().toString()).build();
        request.setEntity(entity);
        return request;
    }
}
