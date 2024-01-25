package com.dedalus.xraycucumber.serviceparameters;

import java.net.URL;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates the parameters needed for interacting with a Jira service.
 */
public class JiraServiceParameters {

    @Nonnull private final URL url;
    private String username;
    private String password;
    private final String projectKey;
    private final String bearerToken;
    private final boolean tokenAuthenticationEnabled;
    private final boolean saveFeatureBeforeUpdate;

    private JiraServiceParameters(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.projectKey = builder.projectKey;
        this.bearerToken = builder.bearerToken;
        this.tokenAuthenticationEnabled = builder.tokenAuthenticationEnabled;
        this.saveFeatureBeforeUpdate = builder.saveFeatureBeforeUpdate;
    }

    public @NotNull URL getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public @NotNull String getProjectKey() {
        return projectKey;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public boolean isTokenAuthenticationEnabled() {
        return tokenAuthenticationEnabled;
    }

    public boolean isSaveFeatureBeforeUpdate() { return saveFeatureBeforeUpdate; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        JiraServiceParameters other = (JiraServiceParameters) obj;

        return Objects.equals(url, other.url) &&
                Objects.equals(username, other.username) &&
                Objects.equals(password, other.password) &&
                Objects.equals(projectKey, other.projectKey) &&
                Objects.equals(bearerToken, other.bearerToken) &&
                tokenAuthenticationEnabled == other.tokenAuthenticationEnabled &&
                saveFeatureBeforeUpdate == other.saveFeatureBeforeUpdate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, projectKey, bearerToken, tokenAuthenticationEnabled, saveFeatureBeforeUpdate);
    }

    public static class Builder {

        private URL url;

        private String username;
        private String password;
        private String projectKey;
        private String bearerToken;
        private boolean tokenAuthenticationEnabled;
        private boolean saveFeatureBeforeUpdate;

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

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public Builder tokenAuthenticationEnabled(boolean tokenAuthenticationEnabled) {
            this.tokenAuthenticationEnabled = tokenAuthenticationEnabled;
            return this;
        }

        public Builder saveFeatureBeforeUpdateEnabled(boolean saveFeatureBeforeUpdate) {
            this.saveFeatureBeforeUpdate = saveFeatureBeforeUpdate;
            return this;
        }

        public JiraServiceParameters build() {
            return new JiraServiceParameters(this);
        }
    }
}