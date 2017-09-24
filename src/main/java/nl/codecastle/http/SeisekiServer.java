package nl.codecastle.http;

import nl.codecastle.configuration.PropertiesReader;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SeisekiServer implements TestLogServer {

    private static final PropertiesReader PROPERTIES_READER = new PropertiesReader("seiseki.properties");
    private static final String LOG_ENDPOINT = PROPERTIES_READER.getValue("server.endpoint") + "/log";
    private static boolean isAvailable = false;
    private final CloseableHttpClient httpClient;

    public SeisekiServer() {
        this(new MultiThreadedHttpClientProvider());
    }

    SeisekiServer(HttpClientProvider httpClientProvider) {
        this.httpClient = httpClientProvider.getHttpClient();
        isAvailable = checkServerAvailability();
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     *
     * @param url     The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     *                the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     */
    public static boolean pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean checkServerAvailability() {
        return pingURL(LOG_ENDPOINT, 2000);
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }
}
