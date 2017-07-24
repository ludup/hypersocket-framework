package com.hypersocket.service;

public interface ServiceStatus {

	String getResourceKey();

	boolean isRunning();

	boolean isError();

	String getErrorText();

	String getGroup();

}