package nl.codecastle.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Provides an implementation of an Apache {@link HttpClient}.
 */
public interface HttpClientProvider {
    /**
     * Returns a build and ready to use Apache {@link HttpClient}.
     *
     * @return the http client
     */
    CloseableHttpClient getHttpClient();
}
