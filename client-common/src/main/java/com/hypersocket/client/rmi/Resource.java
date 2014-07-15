package com.hypersocket.client.rmi;

import java.util.List;

public interface Resource {

	String getHostname();

	List<ResourceProtocol> getProtocols();

	void setResourceRealm(ResourceRealm realm);
	
	ResourceRealm getRealm();

}
