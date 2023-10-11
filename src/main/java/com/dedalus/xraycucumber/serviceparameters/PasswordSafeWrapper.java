package com.dedalus.xraycucumber.serviceparameters;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

/**
 * Wrapper class for interacting with IntelliJ's PasswordSafe for credential management.
 */
public class PasswordSafeWrapper {

    /**
     * Retrieves the stored credentials for the given attributes from the PasswordSafe.
     *
     * @param attributes the attributes defining which credentials to retrieve.
     * @return the stored Credentials, or null if no credentials are stored for the given attributes.
     */
    public Credentials get(CredentialAttributes attributes) {
        return PasswordSafe.getInstance().get(attributes);
    }

    /**
     * Stores the provided credentials in the PasswordSafe.
     * If the credentials parameter is null, removes any stored credentials for the provided attributes.
     *
     * @param attributes the attributes defining where to store the credentials.
     * @param credentials the credentials to store, or null to remove stored credentials.
     */
    public void set(CredentialAttributes attributes, Credentials credentials) {
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    /**
     * Determines if the PasswordSafe is configured to remember passwords by default.
     *
     * @return true if PasswordSafe is configured to remember passwords by default, false otherwise.
     */
    public boolean isRememberPasswordByDefault() {
        return PasswordSafe.getInstance().isRememberPasswordByDefault();
    }
}