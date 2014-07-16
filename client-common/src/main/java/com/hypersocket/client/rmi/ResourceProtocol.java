package com.hypersocket.client.rmi;

public interface ResourceProtocol extends Launchable {

	Long getId();
	
	String getProtocol();

	Resource getResource();

	void setResource(Resource group);

}
