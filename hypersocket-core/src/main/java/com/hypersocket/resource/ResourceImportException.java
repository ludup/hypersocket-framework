package com.hypersocket.resource;

public class ResourceImportException extends ResourceException {

	private static final long serialVersionUID = -2312394108081531142L;

	public ResourceImportException(String bundle, String resourceKey,
			Object... args) {
		super(bundle, resourceKey, args);
	}


}
