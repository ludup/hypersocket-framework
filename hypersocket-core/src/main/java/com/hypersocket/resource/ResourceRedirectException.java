package com.hypersocket.resource;

public class ResourceRedirectException extends ResourcePassthroughException {

	private static final long serialVersionUID = -5649629186555726606L;
	
	private String uri;

	public ResourceRedirectException(Exception redirect, String uri) {
		super(redirect);
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
}
