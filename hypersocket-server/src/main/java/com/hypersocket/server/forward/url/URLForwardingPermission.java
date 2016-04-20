package com.hypersocket.server.forward.url;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.RolePermission;

public enum URLForwardingPermission implements PermissionType {
	
	READ("website.read", RolePermission.READ),
	CREATE("website.create", READ),
	UPDATE("website.update", READ),
	DELETE("website.delete", READ);
	
	private final String val;
	
	private PermissionType[] implies;
	
	private URLForwardingPermission(final String val, PermissionType... implies) {
		this.val = val;
		this.implies = implies;
	}

	@Override
	public PermissionType[] impliesPermissions() {
		return implies;
	}
	
	public String toString() {
		return val;
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
	public boolean isHidden() {
		return false;
	}

}
