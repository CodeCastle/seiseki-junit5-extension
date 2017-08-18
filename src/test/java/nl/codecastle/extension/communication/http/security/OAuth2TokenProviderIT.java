package nl.codecastle.extension.communication.http.security;

import nl.codecastle.extension.communication.http.security.models.AuthorizationError;
import nl.codecastle.extension.communication.http.security.models.OAuth2TokenResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

class OAuth2TokenProviderIT {

    private static ClientAndServer mockServer;
    private OAuth2TokenProvider oAuth2TokenProvider;

    @BeforeAll
    public static void setUpAll() {
        mockServer = startClientAndServer(8080);
    }

    @AfterAll
    public static void tearDownAll() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        oAuth2TokenProvider = new OAuth2TokenProvider();
        mockServer.reset();
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
    }

    private void setupFailedAuthorizationMock() {
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
                                .withStatusCode(401)
                                .withBody("{\"error\":\"invalid_client\",\"error_description\":\"Given client ID does not match authenticated client\"}")
                );
    }

    @Test
    public void shouldReturnAccessToken() throws Exception {
        setupHappyFlowMockServer();
        OAuth2TokenResponse tokenResponse = oAuth2TokenProvider.getToken();

        mockServer.verify(request()
                        .withMethod("POST")
                        .withPath("/uaa/oauth/token"),
                VerificationTimes.exactly(1));

        assertThat(tokenResponse.getAccessToken()).isEqualTo("77d0c19d-a6ac-4e74-8380-e6ab068c39e6");
        assertThat(tokenResponse.getRefreshToken()).isEqualTo("27f3d4e1-5d1b-43e3-b89c-2346bcd6a632");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(43199);
        assertThat(tokenResponse.getScope()).isEqualTo("openid");
        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
    }

    @Test
    public void shouldGoAfterTokenOnlyOnce() throws Exception {
        setupHappyFlowMockServer();
        oAuth2TokenProvider.getToken();
        oAuth2TokenProvider.getToken();

        mockServer.verify(request()
                        .withMethod("POST")
                        .withPath("/uaa/oauth/token"),
                VerificationTimes.exactly(1));
    }

    @Test
    public void shouldGetTokenResponseWithErrorStatus() throws Exception {
        setupFailedAuthorizationMock();
        OAuth2TokenResponse tokenResponse = oAuth2TokenProvider.getToken();

        mockServer.verify(request()
                        .withMethod("POST")
                        .withPath("/uaa/oauth/token"),
                VerificationTimes.exactly(1));

        assertThat(tokenResponse.getAuthorizationError()).isNotNull();
        AuthorizationError error = tokenResponse.getAuthorizationError();
        assertThat(error.getError()).isEqualTo("invalid_client");
        assertThat(error.getErrorDescription()).isEqualTo("Given client ID does not match authenticated client");
    }
}