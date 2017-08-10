package nl.codecastle.extension.model;

/**
 * All possible event types.
 */
public enum TestEventType {
    /**
     * Before all tests of a class are run.
     */
    BEFORE_ALL,
    /**
     * Before the setup of a test is run.
     */
    BEFORE_TEST_SETUP,
    /**
     * After the setup is run but before the execution
     */
    BEFORE_TEST_EXECUTION,
    /**
     * After the test execution.
     */
    AFTER_TEST_EXECUTION,
    /**
     * After both the execution and the tare down.
     */
    AFTER_TEST_TARE_DOWN,
    /**
     * After all the tests of a class are run.
     */
    AFTER_ALL

}
