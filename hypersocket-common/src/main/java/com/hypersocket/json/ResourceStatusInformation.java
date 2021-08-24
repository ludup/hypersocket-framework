package com.hypersocket.json;

public class ResourceStatusInformation<T> extends ResourceStatus<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2313074547196994086L;
	boolean information = false;
	Object[] args;

	public ResourceStatusInformation(String message, Object... args) {
		setSuccess(false);
		setMessage(message);
		this.information = true;
		this.args = args;
	}
	
	public boolean isInformation() {
		return information;
	}
	
	public Object[] getArgs() {
		return args;
	}
	

}
