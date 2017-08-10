package nl.codecastle.extension.communication.http;

import org.apache.http.client.HttpClient;

public interface HttpClientProvider {
    HttpClient getHttpClient();
}
