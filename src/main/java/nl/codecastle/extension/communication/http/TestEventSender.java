package nl.codecastle.extension.communication.http;

import nl.codecastle.extension.model.TestEvent;

public interface TestEventSender {

    void sendEvent(TestEvent testingEvent);
}
