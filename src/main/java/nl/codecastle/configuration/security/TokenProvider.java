package nl.codecastle.configuration.security;

import org.apache.http.auth.AuthenticationException;

import java.io.IOException;

public interface TokenProvider {
    OAuth2TokenResponse getToken() throws IOException, AuthenticationException;
}
