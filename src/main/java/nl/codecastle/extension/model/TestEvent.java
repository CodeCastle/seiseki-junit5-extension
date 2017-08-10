package nl.codecastle.extension.model;

import java.time.LocalDateTime;

/**
 * Object representing the test event.
 */
public class TestEvent {
    /**
     * The time stamp of the event.
     */
    private LocalDateTime localDateTime;
    /**
     * The ID of the project this test belongs to. This project ID should be read from a property file.
     */
    private String projectId;
    /**
     * Each test run has a unique, newly generated ID.
     */
    private String runId;
    /**
     * Name of the test. The name is actually the name of the test method.
     */
    private String testName;
    /**
     * Name of the test class the test belongs to.
     */
    private String className;
    /**
     * The status of the test.
     */
    private String status;
    /**
     * The type of the event.
     */
    private TestEventType type;

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TestEventType getType() {
        return type;
    }

    public void setType(TestEventType type) {
        this.type = type;
    }
}
