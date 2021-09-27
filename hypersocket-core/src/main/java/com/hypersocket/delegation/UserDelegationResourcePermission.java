/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.delegation;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.RolePermission;
import com.hypersocket.realm.UserPermission;


public enum UserDelegationResourcePermission implements PermissionType {
	
	READ("read", RolePermission.READ, UserPermission.READ),
	CREATE("create", READ),
	UPDATE("update", READ),
	DELETE("delete", READ);
	
	private final String val;
	
	/**
	 * TODO place your resource name in this final static string e.g. applications
	 */
	private final static String name = "userDelegation";
	
	private PermissionType[] implies;
	
	private UserDelegationResourcePermission(final String val, PermissionType... implies) {
		this.val = name + "." + val;
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
