package com.hypersocket.resource;

public class ResourceConfirmationException extends ResourcePassthroughException {

	private static final long serialVersionUID = -5649629186555726606L;

	private String[] options;
	
	public ResourceConfirmationException(String bundle, 
			String resourceKey, 
			String[] options,
			Object... args) {
		this(null, bundle, resourceKey, options, args);
	}
	
	public ResourceConfirmationException(
			Throwable cause,
			String bundle, 
			String resourceKey, 
			String[] options,
			Object... args) {
		super(cause, bundle, resourceKey, args);
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}
}
