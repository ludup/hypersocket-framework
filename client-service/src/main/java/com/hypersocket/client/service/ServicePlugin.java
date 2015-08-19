package com.hypersocket.client.service;

import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.rmi.ResourceService;

public interface ServicePlugin {

	boolean start(HypersocketClient<?> serviceClient, ResourceService resourceService, GUIRegistry guiRegistry);

	void stop();

	String getName();

}
