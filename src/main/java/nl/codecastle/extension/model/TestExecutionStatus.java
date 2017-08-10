package nl.codecastle.extension.model;

/**
 * Holds all the possible statuses of a test execution.
 */
public enum TestExecutionStatus {
    /**
     * Indicates that the test run was a success.
     */
    SUCCEDED,
    /**
     * Indicates that the test run was a success.
     */
    FAILED,
    /**
     * Indicates that the setup of the test failed.
     */
    SETUP_FAILED,
    /**
     * Indicates that the tare down of the test failed.
     */
    TAREDOWN_FAILED
}
