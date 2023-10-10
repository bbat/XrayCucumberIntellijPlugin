package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraServiceParameters {

    @Nonnull
    private final URL url;
    private final String username;
    private final String password;
    private final String projectKey;

    private JiraServiceParameters(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.projectKey = builder.projectKey;
    }

    public @NotNull URL getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public @NotNull String getProjectKey() {
        return projectKey;
    }

    public static class Builder {
        private URL url;
        private String username;
        private String password;
        private String projectKey;
        public Builder url(URL url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder projectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public JiraServiceParameters build() {
            return new JiraServiceParameters(this);
        }
    }
}