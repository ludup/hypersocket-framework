/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import com.hypersocket.permissions.PermissionType;

public enum RealmPermission implements PermissionType {

	READ("realm.read"),
	CREATE("realm.create", READ),
	UPDATE("realm.update", READ),
	DELETE("realm.delete", READ),
	SYNCHRONIZE("realm.sync");
	private final String val;
	
	private PermissionType[] implies;
	
	private RealmPermission(final String val, PermissionType... implies) {
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
		return false;
	}
}
