package nl.codecastle.extension.communication.http.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.communication.http.HttpClientProvider;
import nl.codecastle.extension.communication.http.MultiThreadedHttpClientProvider;
import nl.codecastle.extension.communication.http.security.models.AuthorizationError;
import nl.codecastle.extension.communication.http.security.models.OAuth2TokenResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves a security token that will be used in each subsequent request.
 * All necessary data for the request are in the seiseki.properties file.
 */
public class OAuth2TokenProvider implements TokenProvider {

    private static OAuth2TokenResponse oAuth2TokenResponse;
    private final HttpClientProvider clientProvider;
    private final PropertiesReader propertiesReader;

    public OAuth2TokenProvider() {
        this(new MultiThreadedHttpClientProvider(), new PropertiesReader("seiseki.properties"));
    }

    OAuth2TokenProvider(HttpClientProvider clientProvider, PropertiesReader propertiesReader) {
        oAuth2TokenResponse = null;
        this.propertiesReader = propertiesReader;
        this.clientProvider = clientProvider;
    }

    @Override
    public OAuth2TokenResponse getToken() throws IOException {
        if (oAuth2TokenResponse == null) {
            oAuth2TokenResponse = getTokenFromServer();
        }
        return oAuth2TokenResponse;
    }

    private OAuth2TokenResponse getTokenFromServer() throws IOException {
        HttpPost httpPost = new HttpPost(propertiesReader.getValue("server.token.endpoint"));
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                propertiesReader.getValue("client.id"), propertiesReader.getValue("client.secret"));
        OAuth2TokenResponse tokenResponse;
        try {
            tokenResponse = getTokenResponse(httpPost, credentials);
        } catch (AuthenticationException e) {
            tokenResponse = getEmptyResponseWithErrorObject(e);
        }

        return tokenResponse;
    }

    private OAuth2TokenResponse getTokenResponse(HttpPost httpPost, UsernamePasswordCredentials credentials) throws AuthenticationException, IOException {
        OAuth2TokenResponse tokenResponse;
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader(new BasicScheme().authenticate(credentials, httpPost, null));

        httpPost.setEntity(new UrlEncodedFormEntity(getBodyValues()));
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = clientProvider.getHttpClient();
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            tokenResponse = getTokenResponse(response, entity);
        } finally {
            if (response != null) {
                response.close();
            }
        }


        return tokenResponse;
    }

    private OAuth2TokenResponse getEmptyResponseWithErrorObject(AuthenticationException e) {
        OAuth2TokenResponse tokenResponse;
        tokenResponse = new OAuth2TokenResponse();
        AuthorizationError error = new AuthorizationError();
        error.setError("AuthenticationException");
        error.setErrorDescription(e.getMessage());
        return tokenResponse;
    }

    private OAuth2TokenResponse getTokenResponse(HttpResponse response, HttpEntity entity) throws IOException {
        OAuth2TokenResponse tokenResponse;
        if (response.getStatusLine().getStatusCode() == 401) {
            tokenResponse = new OAuth2TokenResponse();
            tokenResponse.setAuthorizationError(getAuthorizationErrorObject(entity));
        } else {
            tokenResponse = getTokenResponseObject(entity);
        }
        return tokenResponse;
    }

    private AuthorizationError getAuthorizationErrorObject(HttpEntity entity) throws IOException {
        String responseString = EntityUtils.toString(entity, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseString, AuthorizationError.class);
    }

    private OAuth2TokenResponse getTokenResponseObject(HttpEntity entity) throws IOException {
        String responseString = EntityUtils.toString(entity, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseString, OAuth2TokenResponse.class);
    }

    private List<NameValuePair> getBodyValues() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", propertiesReader.getValue("username")));
        params.add(new BasicNameValuePair("password", propertiesReader.getValue("password")));
        params.add(new BasicNameValuePair("grant_type", propertiesReader.getValue("client.grant_type")));
        params.add(new BasicNameValuePair("scope", propertiesReader.getValue("client.scope")));
        params.add(new BasicNameValuePair("client_secret", propertiesReader.getValue("client.secret")));
        params.add(new BasicNameValuePair("client_id", propertiesReader.getValue("client.id")));
        return params;
    }
}
