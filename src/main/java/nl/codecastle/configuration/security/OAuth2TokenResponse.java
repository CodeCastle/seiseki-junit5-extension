package nl.codecastle.configuration.security;

public class OAuth2TokenResponse {
    private Object accessToken;

    public Object getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(Object accessToken) {
        this.accessToken = accessToken;
    }
}
