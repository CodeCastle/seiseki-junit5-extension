package nl.codecastle.extension.communication.http;

import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.communication.http.security.OAuth2TokenProvider;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

class SimpleTestEventSenderIT {
    private static final String BODY = "{\"localDateTime\":null,\"projectId\":\"testz\",\"runId\":\"1234\",\"testName\":\"onlyTest\",\"className\":\"SomeTestClass\",\"status\":null,\"type\":\"BEFORE_TEST_EXECUTION\"}";
    private static ClientAndServer mockServer;
    private SimpleTestEventSender simpleTestEventSender;

    @BeforeEach
    void setUp() {
        mockServer = startClientAndServer(8383);
        setupHappyFlowMockServer();
        simpleTestEventSender = new SimpleTestEventSender(new MultiThreadedHttpClientProvider(),
                new OAuth2TokenProvider(), new PropertiesReader("seiseki.properties"));
    }

    @AfterEach
    public void tearDown() {
        mockServer.stop();

    }

    private void setupHappyFlowMockServer() {
        mockServer
                .when(
                        request()
                                .withPath("/uaa/oauth/token")
                                .withBody(exact("username=userOne&password=programirame&grant_type=password&" +
                                        "scope=openid&client_secret=acmesecret&client_id=acme"))
                                .withHeader(new Header("Authorization", "Basic YWNtZTphY21lc2VjcmV0"))
                )
                .respond(
                        response()
                                .withHeaders(new Header(CONTENT_TYPE.toString(), "application/json"))
                                .withBody("{\n" +
                                        "  \"access_token\": \"77d0c19d-a6ac-4e74-8380-e6ab068c39e6\",\n" +
                                        "  \"token_type\": \"bearer\",\n" +
                                        "  \"refresh_token\": \"27f3d4e1-5d1b-43e3-b89c-2346bcd6a632\",\n" +
                                        "  \"expires_in\": 43199,\n" +
                                        "  \"scope\": \"openid\"\n" +
                                        "}")
                );
        mockServer
                .when(
                        request()
                                .withPath("/api")
                                .withBody(exact(BODY))
                                .withHeader(new Header("Authorization", "Bearer 77d0c19d-a6ac-4e74-8380-e6ab068c39e6"))
                )
                .respond(
                        response()
                                .withBody("{}")
                                .withStatusCode(200)
                );
    }

    @Test
    public void shouldSendRequestWhenAuthorized() throws Exception {
        TestEvent testEvent = getTestEvent();

        simpleTestEventSender.sendEvent(testEvent);

        mockServer.verify(
                request()
                        .withPath("/uaa/oauth/token")
                        .withMethod("POST")
                ,
                request()
                        .withPath("/api")
                        .withHeader(new Header("Authorization", "Bearer 77d0c19d-a6ac-4e74-8380-e6ab068c39e6"))
                        .withMethod("POST")
                        .withBody(BODY)

        );
    }

    @Test
    public void shouldGoAfterTokenOnlyOnce() throws Exception {
        TestEvent testEvent = getTestEvent();
        System.out.println("===> Running: " + mockServer.isRunning(50, 60000, TimeUnit.DAYS.MILLISECONDS));
        simpleTestEventSender.sendEvent(testEvent);
        simpleTestEventSender.sendEvent(testEvent);

        mockServer.verify(
                request()
                        .withPath("/uaa/oauth/token")
                        .withMethod("POST")
                ,
                request()
                        .withPath("/api")
                        .withHeader(new Header("Authorization", "Bearer 77d0c19d-a6ac-4e74-8380-e6ab068c39e6"))
                ,
                request()
                        .withPath("/api")
                        .withHeader(new Header("Authorization", "Bearer 77d0c19d-a6ac-4e74-8380-e6ab068c39e6"))

        );
    }

    private TestEvent getTestEvent() {
        TestEvent testEvent = new TestEvent();
        testEvent.setRunId("1234");
        testEvent.setType(TestEventType.BEFORE_TEST_EXECUTION);
        testEvent.setClassName("SomeTestClass");
        testEvent.setProjectId("testz");
        testEvent.setTestName("onlyTest");
        return testEvent;
    }
}