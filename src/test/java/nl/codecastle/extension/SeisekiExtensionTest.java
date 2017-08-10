package nl.codecastle.extension;

import name.falgout.jeffrey.testing.junit5.MockitoExtension;
import nl.codecastle.extension.communication.http.SimpleTestEventSender;
import nl.codecastle.extension.model.TestEvent;
import nl.codecastle.extension.model.TestEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SeisekiExtensionTest {

    private SeisekiExtension rekishi;
    private SimpleTestEventSender testEventSender;
    private DummyTestClass testClass;
    private Optional<Class<?>> optionalMockClass;

    @BeforeEach
    public void setup(@Mock SimpleTestEventSender testEventSender) {
        rekishi = new SeisekiExtension(testEventSender);
        this.testEventSender = testEventSender;
        testClass = new DummyTestClass();
        optionalMockClass = Optional.of(testClass.getClass());
    }

    @Test
    public void afterEach(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.afterEach(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_TEST_TARE_DOWN);

        assertThat(testEvent.getTestName()).isEqualTo("testMethodOne");
    }

    @Test
    public void afterTestExecution(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.afterTestExecution(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_TEST_EXECUTION);

        assertThat(testEvent.getTestName()).isEqualTo("testMethodOne");
    }

    @Test
    public void beforeTestExecution(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.beforeTestExecution(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_TEST_EXECUTION);

        assertThat(testEvent.getTestName()).isEqualTo("testMethodOne");
    }

    @Test
    public void shouldSendBeforeEach(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);
        setupMethodMocks(extensionContext, testClass.getClass());

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.beforeEach(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_TEST_SETUP);

        assertThat(testEvent.getTestName()).isEqualTo("testMethodOne");
    }

    @Test
    public void shouldSendBeforeAllEvent(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.beforeAll(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.BEFORE_ALL);

        assertThat(testEvent.getTestName()).isNullOrEmpty();
    }

    @Test
    public void shouldSendAfterAllEvent(@Mock ExtensionContext extensionContext) throws Exception {
        when(extensionContext.getTestClass()).thenReturn(optionalMockClass);

        ArgumentCaptor<TestEvent> testEventArgumentCaptor = ArgumentCaptor.forClass(TestEvent.class);
        rekishi.afterAll(extensionContext);
        verify(testEventSender).sendEvent(testEventArgumentCaptor.capture());

        TestEvent testEvent = testEventArgumentCaptor.getValue();
        assertGeneralTestEventParameters(testEvent, TestEventType.AFTER_ALL);

        assertThat(testEvent.getTestName()).isNullOrEmpty();
    }

    private void assertGeneralTestEventParameters(TestEvent testEvent, TestEventType testEventType) {
        assertThat(testEvent.getClassName()).isEqualTo(testClass.getClass().getName());
        assertThat(testEvent.getLocalDateTime()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(testEvent.getProjectId()).isEqualTo("kashiki");
        assertThat(testEvent.getType()).isEqualTo(testEventType);
        assertThat(testEvent.getRunId()).isNotEmpty();
    }

    private void setupMethodMocks(@Mock ExtensionContext extensionContext, Class<?> testClassStub) throws NoSuchMethodException {
        Method testMethodOne = testClassStub.getMethod("testMethodOne");
        Method testMethodTwo = testClassStub.getMethod("testMethodTwo");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethodOne), Optional.of(testMethodTwo));
    }
}
