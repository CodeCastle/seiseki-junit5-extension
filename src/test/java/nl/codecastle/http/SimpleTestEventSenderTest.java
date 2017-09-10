package nl.codecastle.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import name.falgout.jeffrey.testing.junit5.MockitoExtension;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import nl.codecastle.http.security.TokenProvider;
import nl.codecastle.http.security.models.AuthorizationError;
import nl.codecastle.http.security.models.OAuth2TokenResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.iterable.Extractor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleTestEventSenderTest {

    private static TestEvent dummyTestEvent;
    private HttpClient httpClientMock;
    private TestEventSender testEventSender;
    private TokenProvider tokenProviderMock;

    @BeforeAll
    public static void setupForAll() {
        dummyTestEvent = getTestEvent("sampleTest", "nl.codecastle.tests.SampleTestClass",
                "seiseki", TestEventType.BEFORE_TEST_EXECUTION, "1224-23gfd-123h");
    }

    private static TestEvent getTestEvent(String sampleTest, String className, String seiseki, TestEventType beforeTestExecution, String runId) {
        TestEvent sampleEvent = new TestEvent();
        sampleEvent.setTestName(sampleTest);
        sampleEvent.setClassName(className);
        sampleEvent.setLocalDateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        sampleEvent.setProjectId(seiseki);
        sampleEvent.setType(beforeTestExecution);
        sampleEvent.setRunId(runId);
        return sampleEvent;
    }

    @BeforeEach
    public void setup(@Mock CloseableHttpClient httpClient, @Mock HttpClientProvider provider, @Mock TokenProvider tokenProvider,
                      @Mock PropertiesReader propertiesReader)
            throws IOException, AuthenticationException {
        tokenProviderMock = tokenProvider;
        httpClientMock = httpClient;
        when(provider.getHttpClient()).thenReturn(httpClient);
        testEventSender = new SimpleTestEventSender(provider, tokenProviderMock, propertiesReader);

        when(propertiesReader.getValue("server.endpoint")).thenReturn("http://testhost:8383/api");
        when(propertiesReader.getValue("server.token.endpoint")).thenReturn("http://localhost:8383/uaa/oauth/token");

        OAuth2TokenResponse oAuth2TokenResponse = mock(OAuth2TokenResponse.class);
        when(oAuth2TokenResponse.getAccessToken()).thenReturn("sample-token-value-123");

        when(tokenProviderMock.getToken()).thenReturn(oAuth2TokenResponse);
    }

    @Test
    public void shouldSendPostWithCorrectContent() throws IOException, UnauthorizedException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);

        testEventSender.sendEvent(dummyTestEvent);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();

        ObjectMapper objectMapper = new ObjectMapper();
        String eventJson = objectMapper.writeValueAsString(dummyTestEvent);

        HttpEntity entity = capturedPost.getEntity();
        String entityContent = EntityUtils.toString(entity);
        assertThat(entityContent).isEqualTo(eventJson);

    }

    @Test
    public void shouldSendPostWithCorrectHeaders() throws IOException, UnauthorizedException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        TestEvent sampleEvent = getTestEvent("sampleTest", "nl.codecastle.tests.SampleTestClass", "seiseki", TestEventType.BEFORE_TEST_EXECUTION, "1224-23gfd-123h");

        testEventSender.sendEvent(sampleEvent);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();

        Header[] allHeaders = capturedPost.getAllHeaders();
        assertThat(allHeaders).extracting((Extractor<Header, Object>) Header::getName).contains("Accept", "Content-type", "Authorization");

        assertThat(getHeaderValue(allHeaders, "Content-type")).isEqualTo("application/json");
        assertThat(getHeaderValue(allHeaders, "Accept")).isEqualTo("application/json");
        assertThat(getHeaderValue(allHeaders, "Authorization")).startsWith("Bearer sample-token-value-123");
    }

    @Test
    public void postShouldBeSentToCorrectEndpoint() throws IOException, UnauthorizedException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        testEventSender.sendEvent(dummyTestEvent);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();
        URI uri = capturedPost.getURI();

        assertThat(uri.getHost()).isEqualTo("testhost");
        assertThat(uri.getPort()).isEqualTo(8383);
    }

    @Test
    public void shouldThrowUnauthorizedException() throws IOException {
        OAuth2TokenResponse oAuth2TokenResponse = mock(OAuth2TokenResponse.class);
        when(oAuth2TokenResponse.getAccessToken()).thenReturn("");

        AuthorizationError error = new AuthorizationError();
        error.setError("error");
        error.setErrorDescription("Unidentified error happened.");

        when(oAuth2TokenResponse.getAuthorizationError()).thenReturn(error);
        when(tokenProviderMock.getToken()).thenReturn(oAuth2TokenResponse);

        Throwable exception = assertThrows(UnauthorizedException.class,
                () -> testEventSender.sendEvent(dummyTestEvent));

        assertThat(exception.getMessage()).isEqualTo("Unauthorized: Unidentified error happened.");
    }

    private String getHeaderValue(Header[] allHeaders, String headerName) {
        String value = "";
        for (Header header : allHeaders) {
            if (header.getName().equals(headerName)) {
                value = header.getValue();
                break;
            }
        }
        assertThat(!value.isEmpty());
        return value;
    }

}