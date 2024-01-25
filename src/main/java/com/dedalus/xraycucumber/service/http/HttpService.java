package com.dedalus.xraycucumber.service.http;

import java.io.IOException;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;

import com.intellij.credentialStore.Credentials;

public class HttpService {

    private final HttpClient httpClient;

    public HttpService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpEntity executeRequest(HttpUriRequest request, Credentials credentials) throws IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        addAuthentication(request, credentials);
        HttpResponse httpResponse = httpClient.execute(request);
        HttpEntity httpEntity = httpResponse.getEntity();

        validateHttpResponse(httpResponse, httpEntity);
        return httpEntity;
    }

    public HttpEntity executeRequest(HttpUriRequest request, String token) throws IOException, AuthenticationException {
        addAuthentication(request, token);
        HttpResponse httpResponse = httpClient.execute(request);
        HttpEntity httpEntity = httpResponse.getEntity();

        validateHttpResponse(httpResponse, httpEntity);
        return httpEntity;
    }

    private void validateHttpResponse(HttpResponse httpResponse, HttpEntity httpEntity) throws IOException, AuthenticationException {
        if (httpResponse == null) {
            throw new IOException("No response from server");
        }

        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine == null) {
            throw new IOException("No status line in server response");
        }

        int statusCode = statusLine.getStatusCode();
        handleStatusCodes(statusCode, httpEntity);
    }

    private void handleStatusCodes(int statusCode, HttpEntity httpEntity) throws AuthenticationException, IllegalStateException, IOException {
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Jira refused authentication (HTTP 401)");
        }

        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT) {
            handleNonOkStatus(httpEntity, statusCode);
        }
    }

    private void handleNonOkStatus(HttpEntity httpEntity, int statusCode) throws IOException {
        // Extracting error message from HTTP entity if possible, otherwise using a default message.
        String message = "Unexpected error";
        if (httpEntity != null) {
            message = EntityUtils.toString(httpEntity);
        }
        throw new IllegalStateException(message + " (HTTP " + statusCode + ")");
    }

    public void addAuthentication(HttpUriRequest request, Credentials credentials) throws AuthenticationException, org.apache.http.auth.AuthenticationException {
        String userName = Optional.ofNullable(credentials.getUserName()).orElseThrow(() -> new AuthenticationException("Username is required"));
        String password = Optional.ofNullable(credentials.getPasswordAsString()).orElseThrow(() -> new AuthenticationException("Password is required"));

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(userName, password);
        request.addHeader(createBasicScheme().authenticate(usernamePasswordCredentials, request, null));
    }

    public void addAuthentication(HttpUriRequest request, String token) {
        request.addHeader("Authorization", "Bearer " + token);
    }

    protected BasicScheme createBasicScheme() {
        return new BasicScheme();
    }
}
