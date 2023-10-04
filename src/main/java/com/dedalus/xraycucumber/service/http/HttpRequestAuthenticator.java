package com.dedalus.xraycucumber.service.http;

import java.util.Optional;

import javax.naming.AuthenticationException;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;

import com.dedalus.xraycucumber.model.ServiceParameters;

public class HttpRequestAuthenticator {

    private final ServiceParameters serviceParameters;

    public HttpRequestAuthenticator(final ServiceParameters serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public void addAuthentication(HttpUriRequest request) throws AuthenticationException, org.apache.http.auth.AuthenticationException {
        String userName = Optional.ofNullable(serviceParameters.getUsername())
                .orElseThrow(() -> new AuthenticationException("Username is required"));
        String password = Optional.ofNullable(serviceParameters.getPassword())
                .orElseThrow(() -> new AuthenticationException("Password is required"));

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(userName, password);
        request.addHeader(new BasicScheme().authenticate(usernamePasswordCredentials, request, null));
    }
}
