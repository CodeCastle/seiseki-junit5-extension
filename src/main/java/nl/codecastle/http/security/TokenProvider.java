package nl.codecastle.http.security;

import nl.codecastle.http.security.models.OAuth2TokenResponse;

import java.io.IOException;

public interface TokenProvider {
    OAuth2TokenResponse getToken() throws IOException;
}
