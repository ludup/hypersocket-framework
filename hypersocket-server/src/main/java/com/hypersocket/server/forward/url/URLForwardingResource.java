package com.hypersocket.server.forward.url;

import com.hypersocket.server.forward.ForwardingResource;

public abstract class URLForwardingResource extends ForwardingResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2801333279313908510L;

	public abstract String getLaunchUrl();

}
