package com.hypersocket.service;

import java.util.Collection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as=ManageableService.class)
public interface ManageableService {

	void stop();
	
	boolean start();
	
	boolean isSystem();
	
	Collection<ServiceStatus> getStatus();
	
}
