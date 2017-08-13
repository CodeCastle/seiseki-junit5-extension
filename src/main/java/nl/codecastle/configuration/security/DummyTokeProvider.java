package nl.codecastle.configuration.security;

import org.apache.http.auth.AuthenticationException;

import java.io.IOException;

public class DummyTokeProvider implements TokenProvider {

    @Override
    public OAuth2TokenResponse getToken() throws IOException, AuthenticationException {
        return new OAuth2TokenResponse();
    }
}