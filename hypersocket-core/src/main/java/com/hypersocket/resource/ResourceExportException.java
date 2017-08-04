package com.hypersocket.resource;

public class ResourceExportException extends ResourceException {

	private static final long serialVersionUID = 7392054503444814524L;

	public ResourceExportException(String bundle, String resourceKey,
			Object... args) {
		super(bundle, resourceKey, args);
	}
	
	public ResourceExportException(String bundle, String resourceKey,
			Throwable cause, Object... args) {
		super(bundle, resourceKey, cause, args);
	}
	
	public ResourceExportException(ResourceException e) {
		super(e);
	}

}
