package nl.codecastle.appenders;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.codecastle.appenders.models.LogEntry;
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

public class HtmlAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlAppender.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TokenProvider tokenProvider = new OAuth2TokenProvider();
    private CloseableHttpClient httpclient;

    public void start() {
        MultiThreadedHttpClientProvider provider = new MultiThreadedHttpClientProvider();
        httpclient = provider.getHttpClient();
        super.start();
    }

    protected void append(ILoggingEvent eventObject) {
        HttpPost httpPost = new HttpPost("http://localhost:8585/uaa/log");

        LogEntry logEntry = new LogEntry();
        logEntry.setClassName(eventObject.getLoggerName());
        logEntry.setTestName(eventObject.getLoggerName());
        logEntry.setLogLine(eventObject.getFormattedMessage());

        try {
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(logEntry));
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            OAuth2TokenResponse token = tokenProvider.getToken();
            if (token.getAuthorizationError() != null) {
                throw new UnauthorizedException("Unauthorized: " + token.getAuthorizationError().getErrorDescription());
            }
            httpPost.setHeader("Authorization", "Bearer " + token.getAccessToken());

            CloseableHttpResponse response = httpclient.execute(httpPost);
            LOG.trace("Log response status: {}", response != null ? response.getStatusLine().getStatusCode() : "");
            if (response != null) {
                response.close();
            }

            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to API", e);
        }
    }
}
