package com.hypersocket.client;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class VariableResult {
	
	boolean success = true;
	String message = "";
	Map<String,String> resource;
	
	public VariableResult() {
	}
	
	public Map<String,String> getResource() {
		return resource;
	}
	
	public void setResource(Map<String,String> resource) {
		this.resource = resource;
	}

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
}
	