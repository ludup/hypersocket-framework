package com.hypersocket.client.gui.jfx;

import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceRealm;

public class ResourceItem {
	private Resource resource;
	private ResourceRealm resourceRealm;

	public ResourceItem(Resource resource, ResourceRealm resourceRealm) {
		this.resource = resource;
		this.resourceRealm = resourceRealm;
	}

	public Resource getResource() {
		return resource;
	}

	public ResourceRealm getResourceRealm() {
		return resourceRealm;
	}

}