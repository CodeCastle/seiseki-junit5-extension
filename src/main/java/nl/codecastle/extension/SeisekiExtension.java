package nl.codecastle.extension;

import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import nl.codecastle.http.MultiThreadedHttpClientProvider;
import nl.codecastle.http.SimpleTestEventSender;
import nl.codecastle.http.TestEventSender;
import nl.codecastle.http.UnauthorizedException;
import nl.codecastle.http.security.OAuth2TokenProvider;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static Logger LOG = LoggerFactory.getLogger(SeisekiExtension.class);
    private static String uuid = UUID.randomUUID().toString();
    private final TestEventSender eventSender;
    private final PropertiesReader propertiesReader;

    public SeisekiExtension() {
        this(new SimpleTestEventSender(new MultiThreadedHttpClientProvider(), new OAuth2TokenProvider(), new PropertiesReader("seiseki.properties")), new PropertiesReader("seiseki.properties"));
    }

    SeisekiExtension(TestEventSender testEventSender, PropertiesReader propertiesReader) {
        this.eventSender = testEventSender;
        this.propertiesReader = propertiesReader;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}]", uuid, className, "AFTER ALL");
        sendTestEvent(extensionContext, TestEventType.AFTER_ALL);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}] [{}]", uuid, className, extensionContext.getDisplayName(), "AFTER EACH");
        sendTestMethodEvent(extensionContext, TestEventType.AFTER_TEST_TARE_DOWN);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}] [{}]", uuid, className, extensionContext.getDisplayName(), "AFTER EXECUTION");
        sendTestMethodEvent(extensionContext, TestEventType.AFTER_TEST_EXECUTION);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}]", uuid, className, "BEFORE ALL");
        sendTestEvent(extensionContext, TestEventType.BEFORE_ALL);
    }

    private void sendTestEvent(ExtensionContext extensionContext, TestEventType eventType)
            throws IOException, UnauthorizedException {

        String className = extensionContext.getTestClass().get().getTypeName();
        eventSender.sendEvent(getClassTestEvent(className, uuid, eventType));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}] [{}]", uuid, className, extensionContext.getDisplayName(), "BEFORE EACH");
        sendTestMethodEvent(extensionContext, TestEventType.BEFORE_TEST_SETUP);
    }

    public static String getUuid() {
        return uuid;
    }

    private void sendTestMethodEvent(ExtensionContext extensionContext, TestEventType eventType)
            throws IOException, UnauthorizedException {

        String className = extensionContext.getTestClass().get().getTypeName();
        String testName = extensionContext.getTestMethod().get().getName();

        eventSender.sendEvent(getMethodTestEvent(className, uuid, eventType, testName));
    }

    private TestEvent getClassTestEvent(String className, String uuid, TestEventType eventType) {
        TestEvent testingEvent = new TestEvent();
        testingEvent.setClassName(className);
        testingEvent.setRunId(uuid);
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        testingEvent.setLocalDateTime(formattedDateTime);
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

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        String className = extensionContext.getTestClass().get().getTypeName();
        LOG.debug("[{}] [{}] [{}] [{}]", uuid, className, extensionContext.getDisplayName(), "BEFORE EXECUTION");
        sendTestMethodEvent(extensionContext, TestEventType.BEFORE_TEST_EXECUTION);
    }
}
