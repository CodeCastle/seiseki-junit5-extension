package nl.codecastle.extension.communication.http;

import nl.codecastle.configuration.PropertiesReader;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Provides a multithread  safe {@link HttpClient} with a {@link PoolingHttpClientConnectionManager} setup.
 * The number of total connections and connections per route are read from a property file.
 */
public class MultiThreadedHttpClientProvider implements HttpClientProvider {
    private static HttpClient httpClient;

    static {
        PropertiesReader reader = new PropertiesReader("seiseki.properties");
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(Integer.parseInt(reader.getValue("client.max.connections")));
        cm.setDefaultMaxPerRoute(Integer.parseInt(reader.getValue("client.max.connections.route")));

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
