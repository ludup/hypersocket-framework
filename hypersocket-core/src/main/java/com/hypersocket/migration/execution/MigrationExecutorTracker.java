package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MigrationExecutorTracker {

    static Logger log = LoggerFactory.getLogger(MigrationExecutorTracker.class);

    private Long success = 0l;
    private Long failure = 0l;
    private Long customOperationSuccess = 0l;
    private Long customOperationFailure = 0l;

    private Map<String, JsonNode> errorNodes = new HashMap();

    public Long getSuccess() {
        return success;
    }

    public void setSuccess(Long success) {
        this.success = success;
    }

    public Long getFailure() {
        return failure;
    }

    public void setFailure(Long failure) {
        this.failure = failure;
    }

    public Map<String, JsonNode> getErrorNodes() {
        return errorNodes;
    }

    public void setErrorNodes(Map<String, JsonNode> errorNodes) {
        this.errorNodes = errorNodes;
    }

    public Long getCustomOperationSuccess() {
        return customOperationSuccess;
    }

    public void setCustomOperationSuccess(Long customOperationSuccess) {
        this.customOperationSuccess = customOperationSuccess;
    }

    public Long getCustomOperationFailure() {
        return customOperationFailure;
    }

    public void setCustomOperationFailure(Long customOperationFailure) {
        this.customOperationFailure = customOperationFailure;
    }

    public void incrementSuccess() {
        this.success++;
    }

    public void incrementFailure() {
        this.failure++;
    }

    public void incrementCustomOperationSuccess() {
        this.customOperationSuccess++;
    }

    public void incrementCustomOperationFailure() {
        this.customOperationFailure++;
    }

    public void addErrorNode(String group, JsonNode jsonNode) {
        errorNodes.put(group, jsonNode);
    }

    public void logErrorNodes() {
        log.error("The error nodes are \n {}", this);
    }

    @Override
    public String toString() {
        return "MigrationExecutorTracker{" +
                "success=" + success +
                ", failure=" + failure +
                ", customOperationSuccess=" + customOperationSuccess +
                ", customOperationFailure=" + customOperationFailure +
                ", errorNodes=" + errorNodes +
                '}';
    }
}
