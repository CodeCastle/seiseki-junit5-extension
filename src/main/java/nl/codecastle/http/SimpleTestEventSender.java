package nl.codecastle.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.http.security.TokenProvider;
import nl.codecastle.http.security.models.OAuth2TokenResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A simple implementation that uses an {@link HttpClientProvider} to retrieve an http client
 * and send all the test events.
 */
public class SimpleTestEventSender implements TestEventSender {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleTestEventSender.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private final TokenProvider tokenProvider;
    private final PropertiesReader propertiesReader;
    private CloseableHttpClient httpClient;
    public SimpleTestEventSender(HttpClientProvider httpClientProvider, TokenProvider tokenProvider,
                                 PropertiesReader propertiesReader) {

        this.httpClient = httpClientProvider.getHttpClient();
        this.tokenProvider = tokenProvider;
        this.propertiesReader = propertiesReader;
    }

    @Override
    public void sendEvent(TestEvent testingEvent) throws IOException, UnauthorizedException {
        HttpPost post = new HttpPost(propertiesReader.getValue("server.endpoint") + "/event");

        String eventJson = objectMapper.writeValueAsString(testingEvent);

        StringEntity entity = new StringEntity(eventJson);
        post.setEntity(entity);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        OAuth2TokenResponse token = tokenProvider.getToken();
        if (token.getAuthorizationError() != null) {
            throw new UnauthorizedException("Unauthorized: " + token.getAuthorizationError().getErrorDescription());
        }
        post.setHeader("Authorization", "Bearer " + token.getAccessToken());
        LOG.trace("Executing post.");
        CloseableHttpResponse response = httpClient.execute(post);
        LOG.debug("Finished executing post!");
        response.close();
    }
}
