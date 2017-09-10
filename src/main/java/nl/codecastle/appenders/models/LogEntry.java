package nl.codecastle.appenders.models;

/**
 * Represents the log entry that is sent to the server.
 */
public class LogEntry {
    private String runId;
    private String className;
    private String testName;
    private String logLine;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getLogLine() {
        return logLine;
    }

    public void setLogLine(String logLine) {
        this.logLine = logLine;
    }
}
