package nl.codecastle.extension;

import name.falgout.jeffrey.testing.junit5.MockitoExtension;
import nl.codecastle.configuration.PropertiesReader;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import nl.codecastle.http.SimpleTestEventSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SeisekiExtensionTest {

    private static final String HTTP_TEST_HOST_8080_API = "http://testhost:8383/api";
    private static final String TEST_METHOD_NAME = "testMethodOne";
    private static final String TEST_PROJECT_NAME = "test";
    private SeisekiExtension seisekiExtension;
    private SimpleTestEventSender testEventSender;
    private DummyTestClass testClass;
    private Optional<Class<?>> optionalMockClass;

    @BeforeEach
    public void setup(@Mock SimpleTestEventSender testEventSender, @Mock PropertiesReader propertiesReader) {
        seisekiExtension = new SeisekiExtension(testEventSender, propertiesReader);
        when(propertiesReader.getValue("server.endpoint")).thenReturn(HTTP_TEST_HOST_8080_API);
        when(propertiesReader.getValue("project.name")).thenReturn(TEST_PROJECT_NAME);
        this.testEventSender = testEventSender;
        testClass = new DummyTestClass();
        optionalMockClass = Optional.of(testClass.getClass());
    }

    @Test
    public void afterEach(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.afterEach(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_TEST_TARE_DOWN);

        assertThat(testEvent.getTestName()).isEqualTo(TEST_METHOD_NAME);
    }

    @Test
    public void afterTestExecution(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.afterTestExecution(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_TEST_EXECUTION);

        assertThat(testEvent.getTestName()).isEqualTo(TEST_METHOD_NAME);
    }

    @Test
    public void beforeTestExecution(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.beforeTestExecution(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_TEST_EXECUTION);

        assertThat(testEvent.getTestName()).isEqualTo(TEST_METHOD_NAME);
    }

    @Test
    public void shouldSendBeforeEach(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.beforeEach(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_TEST_SETUP);

        assertThat(testEvent.getTestName()).isEqualTo(TEST_METHOD_NAME);
    }

    @Test
    public void shouldSendBeforeAllEvent(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.beforeAll(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_ALL);

        assertThat(testEvent.getTestName()).isNullOrEmpty();
    }

    @Test
    public void shouldSendAfterAllEvent(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        seisekiExtension.afterAll(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_ALL);

        assertThat(testEvent.getTestName()).isNullOrEmpty();
    }

    private void assertGeneralTestEventParameters(TestEvent testEvent, TestEventType testEventType) {
        assertThat(testEvent.getClassName()).isEqualTo(testClass.getClass().getName());
        String localDateTime = testEvent.getLocalDateTime();
        LocalDateTime localDateTimeParsed = LocalDateTime.parse(localDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(localDateTimeParsed).isBeforeOrEqualTo(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(testEvent.getProjectId()).isEqualTo(TEST_PROJECT_NAME);
        assertThat(testEvent.getType()).isEqualTo(testEventType);
        assertThat(testEvent.getRunId()).isNotEmpty();
    }

    private void setupMethodMocks(@Mock ExtensionContext extensionContext, Class<?> testClassStub) throws NoSuchMethodException {
        Method testMethodOne = testClassStub.getMethod(TEST_METHOD_NAME);
        Method testMethodTwo = testClassStub.getMethod("testMethodTwo");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethodOne), Optional.of(testMethodTwo));
    }
}
