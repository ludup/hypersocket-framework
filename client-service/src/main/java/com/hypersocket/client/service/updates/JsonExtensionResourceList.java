package com.hypersocket.client.service.updates;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.extensions.ExtensionDefinition;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonExtensionResourceList {

	boolean success;
	String error;
	
	ExtensionDefinition[] resources;
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

	public ExtensionDefinition[] getResources() {
		return resources;
	}

	public void setResources(ExtensionDefinition[] resources) {
		this.resources = resources;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
}
