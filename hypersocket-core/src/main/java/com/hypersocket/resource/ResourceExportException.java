package com.hypersocket.resource;

public class ResourceExportException extends ResourceException {

	private static final long serialVersionUID = 7392054503444814524L;

	public ResourceExportException(String bundle, String resourceKey,
			Object... args) {
		super(bundle, resourceKey, args);
	}
	
	public ResourceExportException(Throwable cause, String bundle, String resourceKey,
			Object... args) {
		super(cause, bundle, resourceKey, args);
	}
	
	public ResourceExportException(ResourceException e) {
		super(e);
	}

}
