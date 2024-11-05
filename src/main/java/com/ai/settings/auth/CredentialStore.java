
package com.ai.settings.auth;

public interface CredentialStore {

    String getPassword(String key);

    String setAndGetPassword(String key, String password);

    static CredentialStore systemCredentialStore() {
        return SystemCredentialStore.INSTANCE;
    }
}
