package nl.codecastle.extension.communication.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.codecastle.extension.communication.http.security.TokenProvider;
import nl.codecastle.extension.communication.http.security.models.OAuth2TokenResponse;
import nl.codecastle.extension.model.TestEvent;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

/**
 * A simple implementation that uses an {@link HttpClientProvider} to retrieve an http client
 * and send all the test events.
 */
public class SimpleTestEventSender implements TestEventSender {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private final TokenProvider tokenProvider;
    private HttpClient httpClient;

    public SimpleTestEventSender(HttpClientProvider httpClientProvider, TokenProvider tokenProvider) {
        this.httpClient = httpClientProvider.getHttpClient();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void sendEvent(TestEvent testingEvent, String endpoint) throws IOException, AuthenticationException {
        HttpPost post = new HttpPost("http://localhost:1008");

        String eventJson = objectMapper.writeValueAsString(testingEvent);

        StringEntity entity = new StringEntity(eventJson);
        post.setEntity(entity);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        OAuth2TokenResponse token = tokenProvider.getToken();
        post.setHeader("Authorization", "Bearer " + token.getAccessToken());
        httpClient.execute(post);
    }
}
