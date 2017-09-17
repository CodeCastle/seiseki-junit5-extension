package nl.codecastle.appenders.models;

import ch.qos.logback.classic.Level;

import java.util.ArrayList;

/**
 * Represents the log entry that is sent to the server.
 */
public class LogEntry {
    private String runId;
    private String logLine;
    private Level level;
    private ArrayList<StackTraceElement> testCalls;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getLogLine() {
        return logLine;
    }

    public void setLogLine(String logLine) {
        this.logLine = logLine;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public ArrayList<StackTraceElement> getTestCalls() {
        return testCalls;
    }

    public void setTestCalls(ArrayList<StackTraceElement> testCalls) {
        this.testCalls = testCalls;
    }
}
