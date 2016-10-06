package com.hypersocket.json;

public class ResourceStatusConfirmation<T> extends ResourceStatus<T> {

	boolean confirmation = false;
	String[] options;
	Object[] args;

	public ResourceStatusConfirmation(String message, String[] options, Object[] args) {
		this.success = false;
		this.confirmation = true;
		this.message = message;
		this.options = options;
		this.args = args;
	}
	
	public String[] getOptions() {
		return options;
	}
	
	public boolean isConfirmation() {
		return confirmation;
	}
	
	public Object[] getArgs() {
		return args;
	}
	

}
