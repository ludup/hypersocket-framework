package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.List;

public class ResourceImpl implements Resource, Serializable {

	private static final long serialVersionUID = 6947909274209893794L;

	String name;
	List<ResourceProtocol> resources;
	ResourceRealm realm;
	
	public ResourceImpl() {
	}

	public ResourceImpl(String name, List<ResourceProtocol> resources) {
		this.name = name;
		this.resources = resources;
		for(ResourceProtocol r : resources) {
			r.setResource(this);
		}
	}
	
	public ResourceImpl(String name) {
		this.name = name;
	}
	
	@Override
	public String getHostname() {
		return name;
	}
	@Override
	public List<ResourceProtocol> getProtocols() {
		return resources;
	}

	@Override
	public void setResourceRealm(ResourceRealm realm) {
		this.realm = realm;
	}

	@Override
	public ResourceRealm getRealm() {
		return realm;
	}
}
