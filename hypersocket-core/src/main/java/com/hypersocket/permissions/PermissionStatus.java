package com.hypersocket.permissions;

public class PermissionStatus {
	
	private final String name;
	private final boolean status;
	
	public PermissionStatus(String name, boolean status) {
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public boolean getStatus() {
		return status;
	}

}
