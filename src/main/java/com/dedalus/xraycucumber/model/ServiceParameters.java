package com.dedalus.xraycucumber.model;

import java.net.URL;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceParameters {

    @Nonnull
    private final URL url;
    private final String username;
    private final String password;
    private final String projectKey;
    private final Long filterId;

    @JsonCreator
    public ServiceParameters(
            @JsonProperty("url") @NotNull URL url,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("projectKey") @NotNull String projectKey,
            @JsonProperty("filterId") Long filterId) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.projectKey = projectKey;
        this.filterId = filterId;
    }

    private ServiceParameters(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.projectKey = builder.projectKey;
        this.filterId = builder.filterId;
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

    public Long getFilterId() {
        return filterId;
    }

    public static class Builder {
        private URL url;
        private String username;
        private String password;
        private String projectKey;
        private Long filterId;
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

        public Builder filterId(Long filterId) {
            this.filterId = filterId;
            return this;
        }
        public ServiceParameters build() {
            return new ServiceParameters(this);
        }
    }
}