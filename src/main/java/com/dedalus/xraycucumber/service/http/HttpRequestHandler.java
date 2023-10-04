package com.dedalus.xraycucumber.service.http;

import java.io.IOException;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;

import com.dedalus.xraycucumber.model.ServiceParameters;

public class HttpRequestHandler {

    private final HttpClient httpClient;
    private final ServiceParameters serviceParameters;

    public HttpRequestHandler(final HttpClient httpClient, final ServiceParameters serviceParameters) {
        this.httpClient = httpClient;
        this.serviceParameters = serviceParameters;
    }

    public HttpEntity executeRequest(HttpUriRequest request) throws IOException, AuthenticationException {
        HttpResponse httpResponse = httpClient.execute(request);
        HttpEntity httpEntity = httpResponse.getEntity();

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Jira refused authentication (HTTP 401)");
        }
        if (statusCode != HttpStatus.SC_OK) {
            ContentType contentType = ContentType.getOrDefault(httpEntity);
            String message = "unexpected error";
            if (contentType.getMimeType().equals(ContentType.TEXT_PLAIN.getMimeType())) {
                message = EntityUtils.toString(httpEntity);
            }
            throw new IllegalStateException(message + " (HTTP " + statusCode + ")");
        }
        return httpEntity;
    }

    public void addAuthentication(HttpUriRequest request) throws AuthenticationException, org.apache.http.auth.AuthenticationException {
        String userName = Optional.ofNullable(serviceParameters.getUsername()).orElseThrow(() -> new AuthenticationException("user is required"));
        String password = Optional.ofNullable(serviceParameters.getPassword()).orElseThrow(() -> new AuthenticationException("password is required"));

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(userName, password);
        request.addHeader(new BasicScheme().authenticate(usernamePasswordCredentials, request, null));
    }
}

