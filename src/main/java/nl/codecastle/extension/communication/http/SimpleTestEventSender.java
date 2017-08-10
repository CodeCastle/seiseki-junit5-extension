package nl.codecastle.extension.communication.http;

import nl.codecastle.extension.model.TestEvent;

/**
 *
 */
public class SimpleTestEventSender implements TestEventSender {
    private HttpClientProvider setHttpClientProvider;

    @Override
    public void sendEvent(TestEvent testingEvent) {

    }

    @Override
    public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.setHttpClientProvider = httpClientProvider;
    }
}
