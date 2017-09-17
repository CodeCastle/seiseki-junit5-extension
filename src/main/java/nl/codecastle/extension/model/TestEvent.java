package nl.codecastle.extension.model;

/**
 * Object representing the test event.
 */

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "localDateTime",
        "projectId",
        "runId",
        "testName",
        "className",
        "status",
        "type",
        "log"
})
public class TestEvent {

    @JsonProperty("localDateTime")
    private String localDateTime;
    @JsonProperty("projectId")
    private String projectId;
    @JsonProperty("runId")
    private String runId;
    @JsonProperty("testName")
    private String testName;
    @JsonProperty("className")
    private String className;
    @JsonProperty("status")
    private Object status;
    @JsonProperty("type")
    private TestEventType type;
    @JsonProperty("log")
    private Object log;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("localDateTime")
    public String getLocalDateTime() {
        return localDateTime;
    }

    @JsonProperty("localDateTime")
    public void setLocalDateTime(String localDateTime) {
        this.localDateTime = localDateTime;
    }

    @JsonProperty("projectId")
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty("projectId")
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("runId")
    public String getRunId() {
        return runId;
    }

    @JsonProperty("runId")
    public void setRunId(String runId) {
        this.runId = runId;
    }

    @JsonProperty("testName")
    public String getTestName() {
        return testName;
    }

    @JsonProperty("testName")
    public void setTestName(String testName) {
        this.testName = testName;
    }

    @JsonProperty("className")
    public String getClassName() {
        return className;
    }

    @JsonProperty("className")
    public void setClassName(String className) {
        this.className = className;
    }

    @JsonProperty("status")
    public Object getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Object status) {
        this.status = status;
    }

    @JsonProperty("type")
    public TestEventType getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(TestEventType type) {
        this.type = type;
    }

    @JsonProperty("log")
    public Object getLog() {
        return log;
    }

    @JsonProperty("log")
    public void setLog(Object log) {
        this.log = log;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
