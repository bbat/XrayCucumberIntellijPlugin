package com.dedalus.xraycucumber.serviceparameters;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

public class PasswordSafeWrapper {

    public Credentials get(CredentialAttributes attributes) {
        return PasswordSafe.getInstance().get(attributes);
    }

    public void set(CredentialAttributes attributes, Credentials credentials) {
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    public boolean isRememberPasswordByDefault() {
        return PasswordSafe.getInstance().isRememberPasswordByDefault();
    }
}