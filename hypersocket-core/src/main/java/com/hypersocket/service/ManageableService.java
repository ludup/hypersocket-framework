package com.hypersocket.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as=ManageableService.class)
public interface ManageableService {

	void stop();
	
	boolean start();
	
	String getResourceKey();
	
	String getResourceBundle();
	
	boolean isRunning();
	
	boolean isError();
	
	String getErrorText();
	
}
