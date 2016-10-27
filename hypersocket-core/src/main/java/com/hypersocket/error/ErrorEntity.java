package com.hypersocket.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorEntity {
    private final String message;
    private final String incidentId;
    private String code;

    public ErrorEntity(String message, String incidentId) {
        this.message = message;
        this.incidentId = incidentId;
    }

    public ErrorEntity(String message, String incidentId, String code) {
        this(message, incidentId);
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getIncidentId() {
        return this.incidentId;
    }

    public String getCode(){
        return this.code;
    }
}
