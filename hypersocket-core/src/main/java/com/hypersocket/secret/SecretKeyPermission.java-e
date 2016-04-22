package com.hypersocket.secret;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.RealmPermission;

public enum SecretKeyPermission implements PermissionType {

	READ("key.read", RealmPermission.READ),
	CREATE("key.create", READ),
	UPDATE("key.update", READ),
	DELETE("key.delete", READ);
	
	private final String val;
	
	private PermissionType[] implies;
	
	private SecretKeyPermission(final String val, PermissionType... implies) {
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
