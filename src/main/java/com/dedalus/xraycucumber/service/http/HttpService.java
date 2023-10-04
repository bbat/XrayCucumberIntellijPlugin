package com.dedalus.xraycucumber.service.http;

import java.io.IOException;

import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

public class HttpService {

    private final HttpClient httpClient;
    private final HttpRequestAuthenticator httpRequestAuthenticator;
    public HttpService(HttpClient httpClient, final HttpRequestAuthenticator httpRequestAuthenticator) {
        this.httpClient = httpClient;
        this.httpRequestAuthenticator = httpRequestAuthenticator;
    }

    public HttpEntity executeRequest(HttpUriRequest request) throws IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        httpRequestAuthenticator.addAuthentication(request);
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

        if (statusCode != HttpStatus.SC_OK) {
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
}
