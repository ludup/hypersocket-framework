package com.hypersocket.server.forward;

import com.hypersocket.resource.AssignableResource;

public abstract class ForwardingResource extends AssignableResource {

	public abstract String getDestinationHostname();
	public abstract String getHostname();

}
