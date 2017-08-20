package nl.codecastle.extension.communication.http.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import name.falgout.jeffrey.testing.junit5.MockitoExtension;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.communication.http.HttpClientProvider;
import nl.codecastle.extension.communication.http.security.models.OAuth2TokenResponse;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Oauth2TokenProviderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private TokenProvider tokenProvider;
    private HttpClientProvider clientProviderMock;

    @BeforeEach
    public void setUpEach(@Mock HttpClientProvider clientProvider, @Mock PropertiesReader propertiesReader) {
        this.clientProviderMock = clientProvider;
        tokenProvider = new OAuth2TokenProvider(clientProvider, propertiesReader);

        setUpMockProperties(propertiesReader);
    }

    private void setUpMockProperties(@Mock PropertiesReader propertiesReader) {
        when(propertiesReader.getValue("server.token.endpoint")).thenReturn("http://localhost:8383/uaa/oauth/token");
        when(propertiesReader.getValue("username")).thenReturn("userOne");
        when(propertiesReader.getValue("password")).thenReturn("programirame");
        when(propertiesReader.getValue("client.grant_type")).thenReturn("password");
        when(propertiesReader.getValue("client.scope")).thenReturn("opeind");
        when(propertiesReader.getValue("client.secret")).thenReturn("acmesecret");
        when(propertiesReader.getValue("client.id")).thenReturn("acme");
    }

    @Test
    public void shouldGetToken(@Mock CloseableHttpClient httpClient, @Mock CloseableHttpResponse response)
            throws IOException, AuthenticationException {

        when(clientProviderMock.getHttpClient()).thenReturn(httpClient);

        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(objectMapper.writeValueAsBytes(sampleTokenResponse())));
        when(response.getEntity()).thenReturn(entity);
        StatusLine statusLineMock = mock(StatusLine.class);
        when(statusLineMock.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLineMock);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(response);
        OAuth2TokenResponse tokenResponse = tokenProvider.getToken();

        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(httpPostArgumentCaptor.capture());

        assertThat(tokenResponse.getAccessToken()).isEqualTo("test-access-token");
        assertThat(tokenResponse.getScope()).isEqualTo("openid");
    }

    public OAuth2TokenResponse sampleTokenResponse() {
        OAuth2TokenResponse tokenResponse = new OAuth2TokenResponse();
        tokenResponse.setAccessToken("test-access-token");
        tokenResponse.setScope("openid");

        return tokenResponse;
    }
}
