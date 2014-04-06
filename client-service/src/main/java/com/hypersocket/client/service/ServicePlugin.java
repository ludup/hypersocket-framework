package com.hypersocket.client.service;

import com.hypersocket.client.HypersocketClient;

public interface ServicePlugin {

	boolean start(HypersocketClient<?> serviceClient);

	void stop();

	String getName();

}
