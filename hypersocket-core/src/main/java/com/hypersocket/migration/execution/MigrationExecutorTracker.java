package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        MapUtils.debugPrint(ps, null, errorNodes);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();

        return "MigrationExecutorTracker{" +
                "success=" + success +
                ", failure=" + failure +
                ", customOperationSuccess=" + customOperationSuccess +
                ", customOperationFailure=" + customOperationFailure +
                ", errorNodes=" + content +
                '}';
    }
}
