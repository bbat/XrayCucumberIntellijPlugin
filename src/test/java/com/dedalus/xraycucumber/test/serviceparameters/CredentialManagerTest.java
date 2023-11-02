package com.dedalus.xraycucumber.test.serviceparameters;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dedalus.xraycucumber.serviceparameters.CredentialManager;
import com.dedalus.xraycucumber.serviceparameters.JiraServiceParameters;
import com.dedalus.xraycucumber.serviceparameters.PasswordSafeWrapper;
import com.dedalus.xraycucumber.ui.dialog.JiraCredentialsDialog;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;

class CredentialManagerTest {

    @Mock private JiraServiceParameters jiraServiceParameters;

    @BeforeEach public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testRetrieveCredentialsFromStoreIfUndefined_withNullCredentials_returnsSameParams() throws MalformedURLException {
        PasswordSafeWrapper passwordSafeWrapper = mock(PasswordSafeWrapper.class);
        when(passwordSafeWrapper.get(any())).thenReturn(null);

        CredentialManager credentialManager = new CredentialManager(passwordSafeWrapper);

        URL testUrl = new URL("http://test-url.com");
        when(jiraServiceParameters.getUrl()).thenReturn(testUrl);

        JiraServiceParameters result = credentialManager.retrieveCredentialsFromStoreIfUndefined(jiraServiceParameters);

        assertSame(result, jiraServiceParameters);
    }

    @Test void testRetrieveCredentialsFromStoreIfUndefined_withNonNullCredentials_returnsModifiedParams() throws MalformedURLException {
        PasswordSafeWrapper passwordSafeWrapper = mock(PasswordSafeWrapper.class);
        when(passwordSafeWrapper.get(any())).thenReturn(new Credentials("username", "password"));

        CredentialManager credentialManager = new CredentialManager(passwordSafeWrapper);

        URL testUrl = new URL("http://test-url.com");
        when(jiraServiceParameters.getUrl()).thenReturn(testUrl);

        JiraServiceParameters result = credentialManager.retrieveCredentialsFromStoreIfUndefined(jiraServiceParameters);

        Assertions.assertNotSame(result, jiraServiceParameters);
        Assertions.assertEquals("username", result.getUsername());
        Assertions.assertEquals("password", result.getPassword());
    }

    @Test void testStoreCredentials_callsPasswordSafeWithCorrectParams() throws MalformedURLException {
        PasswordSafeWrapper passwordSafeWrapper = mock(PasswordSafeWrapper.class);
        CredentialManager credentialManager = new CredentialManager(passwordSafeWrapper);

        URL testUrl = new URL("http://test-url.com");
        when(jiraServiceParameters.getUrl()).thenReturn(testUrl);
        when(jiraServiceParameters.getUsername()).thenReturn("username");
        when(jiraServiceParameters.getPassword()).thenReturn("password");

        credentialManager.storeCredentials(jiraServiceParameters);

        verify(passwordSafeWrapper).set(any(CredentialAttributes.class), any(Credentials.class));
    }

    @Test void testDeleteCredentials_callsPasswordSafeWithCorrectParams() throws MalformedURLException {
        PasswordSafeWrapper passwordSafeWrapper = mock(PasswordSafeWrapper.class);
        CredentialManager credentialManager = new CredentialManager(passwordSafeWrapper);

        URL testUrl = new URL("http://test-url.com");
        when(jiraServiceParameters.getUrl()).thenReturn(testUrl);

        credentialManager.deleteCredentials(jiraServiceParameters);

        verify(passwordSafeWrapper).set(any(CredentialAttributes.class), eq(null));
    }

    @Test void testRequestJiraCredentialsFromUser_userChoosesToStoreCredentials_storesCredentials() throws MalformedURLException {
        PasswordSafeWrapper passwordSafeWrapper = mock(PasswordSafeWrapper.class);
        CredentialManager credentialManager = new CredentialManager(passwordSafeWrapper);

        JiraCredentialsDialog jiraCredentialsDialog = mock(JiraCredentialsDialog.class);

        URL testUrl = new URL("http://test-url.com");
        when(jiraServiceParameters.getUrl()).thenReturn(testUrl);

        when(jiraCredentialsDialog.showAndGet()).thenReturn(true);
        when(jiraCredentialsDialog.getUpdatedServiceParameters()).thenReturn(jiraServiceParameters);
        when(jiraCredentialsDialog.storeCredentials()).thenReturn(true);

        CredentialManager spyCredentialManager = spy(credentialManager);

        doReturn(jiraCredentialsDialog).when(spyCredentialManager).createJiraCredentialsDialog(any(), any());

        JiraServiceParameters result = spyCredentialManager.requestJiraCredentialsFromUser(null, jiraServiceParameters);

        verify(passwordSafeWrapper).set(any(CredentialAttributes.class), any(Credentials.class));
        assertSame(result, jiraServiceParameters);
    }

}
