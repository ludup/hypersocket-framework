package com.hypersocket.resource;

public class ResourceInformationException extends ResourceException {

	private static final long serialVersionUID = -5776058189297880246L;

	public ResourceInformationException(String bundle, 
			String resourceKey, 
			Object... args) {
		this(null, bundle, resourceKey, args);
	}
	
	public ResourceInformationException(
			Throwable cause,
			String bundle, 
			String resourceKey, 
			Object... args) {
		super(cause, bundle, resourceKey, args);
	}
}
