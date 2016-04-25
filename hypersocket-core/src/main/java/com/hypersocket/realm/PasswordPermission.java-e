package com.hypersocket.realm;

import com.hypersocket.permissions.PermissionType;

public enum PasswordPermission implements PermissionType {

	CHANGE("password.change");

	private final String val;

	private PermissionType[] implies;

	private PasswordPermission(final String val,
			PermissionType... implies) {
		this.val = val;
		this.implies = implies;
	}

	@Override
	public String getResourceKey() {
		return val;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public PermissionType[] impliesPermissions() {
		return implies;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

}
