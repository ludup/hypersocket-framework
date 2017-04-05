package com.hypersocket.json;

public class JsonStatusException extends Exception {

	private static final long serialVersionUID = 8359564351774025775L;
	int statusCode;
	
	public JsonStatusException(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
}
