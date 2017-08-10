package nl.codecastle.extension.communication.http;

import org.apache.http.client.HttpClient;

/**
 * Provides an implementation of an Apache {@link HttpClient}.
 */
public interface HttpClientProvider {
    /**
     * Returns a build and ready to use Apache {@link HttpClient}.
     *
     * @return the http client
     */
    HttpClient getHttpClient();
}
