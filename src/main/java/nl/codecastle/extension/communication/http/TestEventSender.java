package nl.codecastle.extension.communication.http;

import nl.codecastle.extension.model.TestEvent;

/**
 * Used to send {@link TestEvent} messages to a server. The server is provided by a {@link HttpClientProvider} object.
 */
public interface TestEventSender {

    /**
     * Sends the given event to the server. The end point where the even will be sent is read from a property file.
     * <br>
     * The same endpoint is used for all types of events.
     *
     * @param testingEvent the test event to send
     */
    void sendEvent(TestEvent testingEvent);

    /**
     * Sets the {@link HttpClientProvider} that will be used to create an {@link org.apache.http.client.HttpClient}.
     *
     * @param httpClientProvider the provider of the http client
     */
    void setHttpClientProvider(HttpClientProvider httpClientProvider);
}
