package com.hypersocket.client.rmi;

import java.util.List;

public interface Resource extends Launchable {

	String getHostname();

	List<ResourceProtocol> getProtocols();

	void setResourceRealm(ResourceRealm realm);
	
	ResourceRealm getRealm();

	String getName();

}
