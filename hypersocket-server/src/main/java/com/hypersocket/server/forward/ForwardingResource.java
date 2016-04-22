package com.hypersocket.server.forward;

import com.hypersocket.resource.AssignableResource;

public abstract class ForwardingResource extends AssignableResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3038800865056046091L;
	public abstract String getDestinationHostname();
	public abstract String getHostname();

}
