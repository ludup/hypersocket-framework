package com.hypersocket.json;

public class ResourceStatusConfirmation<T> extends ResourceStatus<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2726922042883083215L;
	private boolean confirmation = false;
	private String[] options;
	private Object[] args;

	public ResourceStatusConfirmation(String message, String[] options, Object[] args) {
		setSuccess(false);
		setMessage(message);
		this.confirmation = true;
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
