package com.hypersocket.json;

public class ResourceStatusInformation<T> extends ResourceStatus<T> {

	boolean information = false;
	Object[] args;

	public ResourceStatusInformation(String message, Object[] args) {
		this.success = false;
		this.information = true;
		this.message = message;
		this.args = args;
	}
	
	public boolean isInformation() {
		return information;
	}
	
	public Object[] getArgs() {
		return args;
	}
	

}
