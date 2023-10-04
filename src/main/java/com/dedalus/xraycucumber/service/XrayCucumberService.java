package com.dedalus.xraycucumber.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.exception.ExceptionHandler;
import com.dedalus.xraycucumber.service.http.HttpEntityProcessor;
import com.dedalus.xraycucumber.service.http.HttpRequestHandler;
import com.dedalus.xraycucumber.service.request.FeatureUploadRequestBuilder;
import com.google.gson.JsonArray;

public class XrayCucumberService {

    private final HttpClient httpClient;
    private final ServiceParameters serviceParameters;

    public XrayCucumberService(HttpClient httpClient, final ServiceParameters serviceParameters) {
        this.httpClient = httpClient;
        this.serviceParameters = serviceParameters;
    }

    public JsonArray uploadXrayCucumberTest(Path featureFile, ProgressReporter progressReporter) {
        JsonArray result = null;
        ExceptionHandler exceptionHandler = new ExceptionHandler();

        if (progressReporter == null) {
            throw new RuntimeException("reporter is null");
        }

        try {
            HttpEntity httpEntity = executeUpload(featureFile);
            progressReporter.reportSuccess("Uploaded successfully " + featureFile);
            HttpEntityProcessor httpEntityProcessor = new HttpEntityProcessor();
            result = httpEntityProcessor.processResponse(httpEntity, httpClient, serviceParameters);

        } catch (AuthenticationException e) {
            exceptionHandler.handle(e, progressReporter, "Service Authentication Error: " + e.getMessage());

        } catch (IllegalArgumentException | IllegalStateException | URISyntaxException | IOException e) {
            exceptionHandler.handle(e, progressReporter, "Upload Error: " + e.getMessage());

        } catch (org.apache.http.auth.AuthenticationException e) {
            exceptionHandler.handle(e, progressReporter, "Jira Authentication Error: " + e.getMessage());

        }

        return result;
    }

    private HttpEntity executeUpload(Path featureFile)
            throws AuthenticationException, URISyntaxException, IOException, org.apache.http.auth.AuthenticationException {
        FeatureUploadRequestBuilder featureUploadRequestBuilder = new FeatureUploadRequestBuilder(serviceParameters);
        HttpUriRequest request = featureUploadRequestBuilder.build(featureFile, httpClient);
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(httpClient, serviceParameters);
        return httpRequestHandler.executeRequest(request);
    }
}
