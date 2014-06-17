package com.hypersocket.service;

public interface ManageableService {

	
	void stop();
	
	void start();
	
	String getResourceKey();
	
	String getResourceBundle();
	
	boolean isRunning();
	
}
