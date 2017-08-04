package com.hypersocket.resource;

public class ResourceConfirmationException extends ResourceException {

	private static final long serialVersionUID = -5649629186555726606L;

	String[] options;
	
	public ResourceConfirmationException(String bundle, 
			String resourceKey, 
			String[] options,
			Object... args) {
		this(bundle, resourceKey, options, null, args);
	}
	
	public ResourceConfirmationException(String bundle, 
			String resourceKey, 
			String[] options,
			Throwable cause,
			Object... args) {
		super(bundle, resourceKey, cause, args);
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}
}
