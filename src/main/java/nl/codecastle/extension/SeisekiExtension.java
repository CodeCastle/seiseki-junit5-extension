package nl.codecastle.extension;

import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.communication.http.MultiThreadedHttpClientProvider;
import nl.codecastle.extension.communication.http.SimpleTestEventSender;
import nl.codecastle.extension.communication.http.TestEventSender;
import nl.codecastle.extension.communication.http.security.OAuth2TokenProvider;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import org.apache.http.auth.AuthenticationException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The Seiseki extension sends events to a server.
 *
 * The server endpoint is retrieved from a 'project.properties' file found in the resource folder of the test
 * project. If that file is missing then a default properties file is found inside the Seiseki library.
 *
 */
public class SeisekiExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static String uuid = UUID.randomUUID().toString();
    private final TestEventSender eventSender;
    private final PropertiesReader propertiesReader;

    public SeisekiExtension() {
        this(new SimpleTestEventSender(new MultiThreadedHttpClientProvider(), new OAuth2TokenProvider()), new PropertiesReader("seiseki.properties"));
    }

    SeisekiExtension(TestEventSender testEventSender, PropertiesReader propertiesReader) {
        this.eventSender = testEventSender;
        this.propertiesReader = propertiesReader;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        sendTestEvent(extensionContext, TestEventType.AFTER_ALL);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        sendTestMethodEvent(extensionContext, TestEventType.AFTER_TEST_TARE_DOWN);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        sendTestMethodEvent(extensionContext, TestEventType.AFTER_TEST_EXECUTION);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        sendTestEvent(extensionContext, TestEventType.BEFORE_ALL);
    }

    private void sendTestEvent(ExtensionContext extensionContext, TestEventType eventType)
            throws IOException, AuthenticationException {

        String className = extensionContext.getTestClass().get().getName();
        eventSender.sendEvent(getTestEvent(className, uuid, eventType), propertiesReader.getValue("server.endpoint"));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        sendTestMethodEvent(extensionContext, TestEventType.BEFORE_TEST_SETUP);
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        sendTestMethodEvent(extensionContext, TestEventType.BEFORE_TEST_EXECUTION);
    }

    private void sendTestMethodEvent(ExtensionContext extensionContext, TestEventType eventType)
            throws IOException, AuthenticationException {

        String className = extensionContext.getTestClass().get().getName();
        String testName = extensionContext.getTestMethod().get().getName();

        eventSender.sendEvent(getTestEvent(className, uuid, eventType, testName), propertiesReader.getValue("server.endpoint"));
    }

    private TestEvent getTestEvent(String className, String uuid, TestEventType eventType) {
        TestEvent testingEvent = new TestEvent();
        testingEvent.setClassName(className);
        testingEvent.setRunId(uuid);
        testingEvent.setLocalDateTime(LocalDateTime.now());
        testingEvent.setProjectId(propertiesReader.getValue("project.name"));
        testingEvent.setClassName(className);
        testingEvent.setType(eventType);
        return testingEvent;
    }

    private TestEvent getTestEvent(String className, String uuid, TestEventType eventType, String methodName) {
        TestEvent testingEvent = getTestEvent(className, uuid, eventType);
        testingEvent.setTestName(methodName);
        return testingEvent;
    }
}
