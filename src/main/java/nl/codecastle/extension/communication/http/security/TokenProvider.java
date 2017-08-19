package nl.codecastle.extension.communication.http.security;

import nl.codecastle.extension.communication.http.security.models.OAuth2TokenResponse;

import java.io.IOException;

public interface TokenProvider {
    OAuth2TokenResponse getToken() throws IOException;
}
