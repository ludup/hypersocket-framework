package com.hypersocket.client.service.browser;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonBrowserResourceList {

	boolean success;
	String error;
	
	JsonBrowserResource[] resources;
	Map<String,String> properties;
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public JsonBrowserResource[] getResources() {
		return resources;
	}

	public void setResources(JsonBrowserResource[] resources) {
		this.resources = resources;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
}
