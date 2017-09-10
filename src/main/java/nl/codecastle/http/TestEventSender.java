package nl.codecastle.http;

import nl.codecastle.extension.model.TestEvent;

import java.io.IOException;

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
    void sendEvent(TestEvent testingEvent) throws IOException, UnauthorizedException;
}
