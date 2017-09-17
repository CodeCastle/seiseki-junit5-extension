package nl.codecastle.appenders;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.codecastle.appenders.models.LogEntry;
import nl.codecastle.extension.SeisekiExtension;
import nl.codecastle.http.MultiThreadedHttpClientProvider;
import nl.codecastle.http.UnauthorizedException;
import nl.codecastle.http.security.OAuth2TokenProvider;
import nl.codecastle.http.security.TokenProvider;
import nl.codecastle.http.security.models.OAuth2TokenResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * {@link AppenderBase} extension that sends all the logs to the server.
 */
public class HtmlAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlAppender.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TokenProvider tokenProvider = new OAuth2TokenProvider();
    private CloseableHttpClient httpClient;
    private OAuth2TokenResponse oAuth2TokenResponse;
    private boolean authorized = true;

    @Override
    public void start() {
        MultiThreadedHttpClientProvider provider = new MultiThreadedHttpClientProvider();
        httpClient = provider.getHttpClient();
        try {
            oAuth2TokenResponse = getOAuth2TokenResponse();
        } catch (UnauthorizedException e) {
            authorized = false;
            LOG.error("Unauthorized access to API", e);
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (authorized) {
            sendLogToServer(eventObject);
        }
    }

    /**
     * Sends the log to the server.
     *
     * @param eventObject the log event received from logback
     */
    private void sendLogToServer(ILoggingEvent eventObject) {
        HttpPost httpPost = new HttpPost("http://localhost:8585/uaa/log");
        ArrayList<StackTraceElement> testCalls = getStackTraceElements(eventObject);

        LogEntry logEntry = new LogEntry();
        logEntry.setLogLine(eventObject.getFormattedMessage());
        logEntry.setLevel(eventObject.getLevel());
        logEntry.setRunId(SeisekiExtension.getUuid());
        logEntry.setTestCalls(testCalls);

        try {
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(logEntry));
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + oAuth2TokenResponse.getAccessToken());

            LOG.trace("Posting a log.");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            LOG.trace("Log response status: {}", response != null ? response.getStatusLine().getStatusCode() : "");
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<StackTraceElement> getStackTraceElements(ILoggingEvent eventObject) {
        StackTraceElement[] stackTraceElements = eventObject.getCallerData();
        ArrayList<StackTraceElement> testCalls = new ArrayList<>();

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (!stackTraceElement.isNativeMethod()) {
                testCalls.add(stackTraceElement);
            } else {
                break;
            }
        }
        return testCalls;
    }

    private OAuth2TokenResponse getOAuth2TokenResponse() throws UnauthorizedException {
        OAuth2TokenResponse token = null;
        try {
            token = tokenProvider.getToken();

            if (token.getAuthorizationError() != null) {
                throw new UnauthorizedException("Unauthorized: " + token.getAuthorizationError().getErrorDescription());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }
}
