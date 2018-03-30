package com.hypersocket.services.json;

import com.hypersocket.service.ServiceStatus;

public class StatusView implements ServiceStatus {

	String resourceKey;
	boolean isRunning;
	boolean isError;
	String errorText;
	String group;
	public StatusView(ServiceStatus status) {
		this.resourceKey = status.getResourceKey();
		this.isRunning = status.isRunning();
		this.isError = status.isError();
		this.errorText = status.getErrorText();
		this.group = status.getGroup();
	}

	@Override
	public String getResourceKey() {
		return resourceKey;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public boolean isError() {
		return isError;
	}

	@Override
	public String getErrorText() {
		return errorText;
	}

	@Override
	public String getGroup() {
		return group;
	}

	
}