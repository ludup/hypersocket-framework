/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

public enum SystemPermission implements PermissionType {

	SYSTEM_ADMINISTRATION("system.administration", false),
	SWITCH_REALM("switchRealm.permission", true),
	SYSTEM("system.permission", true);
	
	private final String val;
	private boolean hidden;
	private PermissionType[] implies;
	
	private SystemPermission(final String val, boolean hidden, PermissionType... implies) {
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
		return true;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}
}
