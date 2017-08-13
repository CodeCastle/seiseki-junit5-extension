package nl.codecastle.extension.communication.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import name.falgout.jeffrey.testing.junit5.MockitoExtension;
import nl.codecastle.OAuth2TokenResponse;
import nl.codecastle.configuration.security.TokenProvider;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleTestEventSenderTest {

    private static final String LOCALHOST = "localhost";
    private static final int PORT = 1008;
    private static final String SERVER_EVENT_URL = "http://" + LOCALHOST + ":" + PORT;
    private static TestEvent dummyTestEvent;
    private HttpClient httpClientMock;
    private TestEventSender testEventSender;

    @BeforeAll
    public static void setupForAll() {
        dummyTestEvent = getTestEvent("sampleTest", "nl.codecastle.tests.SampleTestClass",
                "seiseki", TestEventType.BEFORE_TEST_EXECUTION, "1224-23gfd-123h");
    }

    private static TestEvent getTestEvent(String sampleTest, String className, String seiseki, TestEventType beforeTestExecution, String runId) {
        TestEvent sampleEvent = new TestEvent();
        sampleEvent.setTestName(sampleTest);
        sampleEvent.setClassName(className);
        sampleEvent.setLocalDateTime(LocalDateTime.now());
        sampleEvent.setProjectId(seiseki);
        sampleEvent.setType(beforeTestExecution);
        sampleEvent.setRunId(runId);
        return sampleEvent;
    }

    @BeforeEach
    public void setup(@Mock HttpClient httpClient, @Mock HttpClientProvider provider, @Mock TokenProvider tokenProvider)
            throws IOException, AuthenticationException {

        httpClientMock = httpClient;
        when(provider.getHttpClient()).thenReturn(httpClient);
        testEventSender = new SimpleTestEventSender(provider, tokenProvider);

        OAuth2TokenResponse oAuth2TokenResponse = mock(OAuth2TokenResponse.class);
        when(oAuth2TokenResponse.getAccessToken()).thenReturn("sample-token-value-123");

        when(tokenProvider.getToken()).thenReturn(oAuth2TokenResponse);
    }

    @Test
    public void shouldSendPostWithCorrectContent() throws IOException, AuthenticationException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);

        testEventSender.sendEvent(dummyTestEvent, SERVER_EVENT_URL);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();

        ObjectMapper objectMapper = new ObjectMapper();
        String eventJson = objectMapper.writeValueAsString(dummyTestEvent);

        HttpEntity entity = capturedPost.getEntity();
        String entityContent = EntityUtils.toString(entity);
        assertThat(entityContent).isEqualTo(eventJson);

    }

    @Test
    public void shouldSendPostWithCorrectHeaders() throws IOException, AuthenticationException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        TestEvent sampleEvent = getTestEvent("sampleTest", "nl.codecastle.tests.SampleTestClass", "seiseki", TestEventType.BEFORE_TEST_EXECUTION, "1224-23gfd-123h");

        testEventSender.sendEvent(sampleEvent, SERVER_EVENT_URL);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();

        Header[] allHeaders = capturedPost.getAllHeaders();
        assertThat(allHeaders).extracting((Extractor<Header, Object>) Header::getName).contains("Accept", "Content-type", "Authorization");

        assertThat(getHeaderValue(allHeaders, "Content-type")).isEqualTo("application/json");
        assertThat(getHeaderValue(allHeaders, "Accept")).isEqualTo("application/json");
        assertThat(getHeaderValue(allHeaders, "Authorization")).startsWith("Bearer sample-token-value-123");
    }

    @Test
    public void postShouldBeSentToCorrectEndpoint() throws IOException, AuthenticationException {
        ArgumentCaptor<HttpPost> httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        testEventSender.sendEvent(dummyTestEvent, SERVER_EVENT_URL);

        verify(httpClientMock).execute(httpPostArgumentCaptor.capture());
        HttpPost capturedPost = httpPostArgumentCaptor.getValue();
        URI uri = capturedPost.getURI();

        assertThat(uri.getHost()).isEqualTo(LOCALHOST);
        assertThat(uri.getPort()).isEqualTo(PORT);
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