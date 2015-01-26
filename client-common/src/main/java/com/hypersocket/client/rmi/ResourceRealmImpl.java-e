package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.List;

public class ResourceRealmImpl implements ResourceRealm, Serializable {

	private static final long serialVersionUID = -2321878064950104362L;

	String name;
	List<Resource> resources;
	
	public ResourceRealmImpl() {
	}

	public ResourceRealmImpl(String name, List<Resource> resources) {
		this.name = name;
		this.resources = resources;
		
		for(Resource r : resources) {
			r.setResourceRealm(this);
		}
	}

	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Resource> getResources() {
		return resources;
	}

	@Override
	public void addResource(ResourceImpl res) {
		res.setResourceRealm(this);
		resources.add(res);
	}

}
