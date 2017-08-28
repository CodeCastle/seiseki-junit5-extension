package nl.codecastle.extension;

import nl.codecastle.collector.LogCollector;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.communication.http.MultiThreadedHttpClientProvider;
import nl.codecastle.extension.communication.http.SimpleTestEventSender;
import nl.codecastle.extension.communication.http.TestEventSender;
import nl.codecastle.extension.communication.http.UnauthorizedException;
import nl.codecastle.extension.communication.http.security.OAuth2TokenProvider;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
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
    private LogCollector logCollector = new LogCollector();

    public SeisekiExtension() {
        this(new SimpleTestEventSender(new MultiThreadedHttpClientProvider(), new OAuth2TokenProvider(), new PropertiesReader("seiseki.properties")), new PropertiesReader("seiseki.properties"));
    }

    SeisekiExtension(TestEventSender testEventSender, PropertiesReader propertiesReader) {
        this.eventSender = testEventSender;
        this.propertiesReader = propertiesReader;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        System.out.println("Ending collection!");
        sendLoggedTestEvent(extensionContext, TestEventType.AFTER_ALL, logCollector.stop());
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
        logCollector.start();
        sendTestEvent(extensionContext, TestEventType.BEFORE_ALL);
    }

    private void sendTestEvent(ExtensionContext extensionContext, TestEventType eventType)
            throws IOException, UnauthorizedException {

        String className = extensionContext.getTestClass().get().getName();
        eventSender.sendEvent(getClassTestEvent(className, uuid, eventType));
    }

    private void sendLoggedTestEvent(ExtensionContext extensionContext, TestEventType eventType, String log)
            throws IOException, UnauthorizedException {

        String className = extensionContext.getTestClass().get().getName();
        eventSender.sendEvent(getLoggedClassEvent(className, uuid, eventType, log));
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
            throws IOException, UnauthorizedException {

        String className = extensionContext.getTestClass().get().getName();
        String testName = extensionContext.getTestMethod().get().getName();

        eventSender.sendEvent(getMethodTestEvent(className, uuid, eventType, testName));
    }

    private TestEvent getClassTestEvent(String className, String uuid, TestEventType eventType) {
        TestEvent testingEvent = new TestEvent();
        testingEvent.setClassName(className);
        testingEvent.setRunId(uuid);
        testingEvent.setLocalDateTime(LocalDateTime.now());
        testingEvent.setProjectId(propertiesReader.getValue("project.name"));
        testingEvent.setClassName(className);
        testingEvent.setType(eventType);
        return testingEvent;
    }

    private TestEvent getLoggedClassEvent(String className, String uuid, TestEventType eventType, String log) {
        TestEvent testingEvent = getClassTestEvent(className, uuid, eventType);
        testingEvent.setLog(log);
        return testingEvent;
    }

    private TestEvent getMethodTestEvent(String className, String uuid, TestEventType eventType, String methodName) {
        TestEvent testingEvent = getClassTestEvent(className, uuid, eventType);
        testingEvent.setTestName(methodName);
        return testingEvent;
    }
}
