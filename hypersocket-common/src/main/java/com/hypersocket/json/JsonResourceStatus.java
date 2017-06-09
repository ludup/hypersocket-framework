package com.hypersocket.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonResourceStatus {

	boolean success;
	String message;
	
	JsonResource resource;
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonResource getResource() {
		return resource;
	}

	public void setResource(JsonResource resource) {
		this.resource = resource;
	}
	
}
