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

import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;

/**
 * This service provides HTTP operations specific to the Jira integration, including authentication and request execution.
 */
public class HttpService {

    private final HttpClient httpClient;
    private final JiraServiceParameters jiraServiceParameters;

    /**
     * Constructs an HttpService instance with the provided HTTP client and service parameters.
     *
     * @param httpClient the HTTP client for executing requests.
     * @param jiraServiceParameters1 the parameters needed for Jira service.
     */
    public HttpService(HttpClient httpClient, final JiraServiceParameters jiraServiceParameters1) {
        this.httpClient = httpClient;
        this.jiraServiceParameters = jiraServiceParameters1;
    }

    /**
     * Executes an HTTP request, handling authentication and response validation.
     *
     * @param request the HTTP request to execute.
     * @return the HTTP entity containing the response data.
     * @throws IOException if an error occurs during request execution.
     * @throws AuthenticationException if authentication is refused by Jira.
     * @throws org.apache.http.auth.AuthenticationException if an error occurs during authentication.
     */
    public HttpEntity executeRequest(HttpUriRequest request) throws IOException, AuthenticationException, org.apache.http.auth.AuthenticationException {
        addAuthentication(request, jiraServiceParameters);
        HttpResponse httpResponse = httpClient.execute(request);
        HttpEntity httpEntity = httpResponse.getEntity();

        validateHttpResponse(httpResponse, httpEntity);
        return httpEntity;
    }

    /**
     * Validates the HTTP response, checking for null responses and status codes.
     *
     * @param httpResponse the response to validate.
     * @param httpEntity the entity contained in the response.
     * @throws IOException if the response or its status line is null.
     * @throws AuthenticationException if the response indicates an authentication error.
     */
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

    /**
     * Checks for specific status codes and handles the responses accordingly.
     *
     * @param statusCode the status code of the HTTP response.
     * @param httpEntity the entity contained in the response.
     * @throws AuthenticationException if the status code indicates an authentication error.
     * @throws IllegalStateException if the status code indicates a non-OK status.
     * @throws IOException if there's an issue extracting error messages.
     */
    private void handleStatusCodes(int statusCode, HttpEntity httpEntity) throws AuthenticationException, IllegalStateException, IOException {
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Jira refused authentication (HTTP 401)");
        }

        if (statusCode != HttpStatus.SC_OK) {
            handleNonOkStatus(httpEntity, statusCode);
        }
    }

    /**
     * Handles non-OK status codes, extracting error messages and throwing exceptions.
     *
     * @param httpEntity the entity contained in the response.
     * @param statusCode the status code of the HTTP response.
     * @throws IOException if there's an issue extracting error messages.
     */
    private void handleNonOkStatus(HttpEntity httpEntity, int statusCode) throws IOException {
        // Extracting error message from HTTP entity if possible, otherwise using a default message.
        String message = "Unexpected error";
        if (httpEntity != null) {
            message = EntityUtils.toString(httpEntity);
        }
        throw new IllegalStateException(message + " (HTTP " + statusCode + ")");
    }

    /**
     * Adds authentication headers to the provided HTTP request using the provided service parameters.
     *
     * @param request the HTTP request to which authentication will be added.
     * @param serviceParameters the service parameters containing authentication details.
     * @throws AuthenticationException if username or password is missing.
     * @throws org.apache.http.auth.AuthenticationException if there's an issue with authentication.
     */
    public void addAuthentication(HttpUriRequest request, JiraServiceParameters serviceParameters) throws AuthenticationException, org.apache.http.auth.AuthenticationException {
        String userName = Optional.ofNullable(serviceParameters.getUsername()).orElseThrow(() -> new AuthenticationException("Username is required"));
        String password = Optional.ofNullable(serviceParameters.getPassword()).orElseThrow(() -> new AuthenticationException("Password is required"));

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(userName, password);
        request.addHeader(createBasicScheme().authenticate(usernamePasswordCredentials, request, null));
    }

    /**
     * Creates and returns a new BasicScheme for HTTP authentication.
     *
     * @return a new instance of BasicScheme.
     */
    protected BasicScheme createBasicScheme() {
        return new BasicScheme();
    }
}
