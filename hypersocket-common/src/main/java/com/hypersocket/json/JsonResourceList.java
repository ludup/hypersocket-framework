package com.hypersocket.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonResourceList<T extends JsonResource> {

	boolean success;
	String error;
	
	T[] resources;
	
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

	public T[] getResources() {
		return resources;
	}

	public void setResources(T[] resources) {
		this.resources = resources;
	}
	
	
	
}
