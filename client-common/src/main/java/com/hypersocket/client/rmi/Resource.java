package com.hypersocket.client.rmi;

import java.util.List;

public interface Resource extends Launchable {
	
	public enum Type {
		SSO, BROWSER, FILE, NETWORK
	}
	
	String getIcon();
	
	String getColour();
	
	Type getType();

	String getHostname();

	List<ResourceProtocol> getProtocols();

	void setResourceRealm(ResourceRealm realm);
	
	ResourceRealm getRealm();

	String getName();

}
