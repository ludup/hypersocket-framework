package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceImpl implements Resource, Serializable {

	private static final long serialVersionUID = 6947909274209893794L;

	String name;
	List<ResourceProtocol> protocols;
	ResourceRealm realm;
	boolean launchable;
	ResourceLauncher launcher;
	
	public ResourceImpl() {
	}

	public ResourceImpl(String name, List<ResourceProtocol> resources) {
		this.name = name;
		this.protocols = resources;
		for(ResourceProtocol r : resources) {
			r.setResource(this);
		}
	}
	
	public ResourceImpl(String name) {
		this.name = name;
		this.protocols = new ArrayList<ResourceProtocol>();
	}
	
	public void addProtocol(ResourceProtocol proto) {
		proto.setResource(this);
		protocols.add(proto);
	}
	
	@Override
	public String getHostname() {
		return name;
	}
	@Override
	public List<ResourceProtocol> getProtocols() {
		return protocols;
	}

	@Override
	public void setResourceRealm(ResourceRealm realm) {
		this.realm = realm;
	}

	@Override
	public ResourceRealm getRealm() {
		return realm;
	}

	@Override
	public boolean isLaunchable() {
		return launchable;
	}
	
	@Override
	public void setLaunchable(boolean launchable) {
		this.launchable = launchable;
	}

	@Override
	public ResourceLauncher getResourceLauncher() {
		return launcher;
	}

	@Override
	public void setResourceLauncher(ResourceLauncher launcher) {
		this.launcher = launcher;
	}

	@Override
	public String getName() {
		return name;
	}
}
