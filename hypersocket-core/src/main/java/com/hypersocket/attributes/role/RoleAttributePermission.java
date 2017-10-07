/*******************************************************************************
 * Copyright (c) 2013-2015 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.attributes.role;

import com.hypersocket.permissions.PermissionType;

public enum RoleAttributePermission implements PermissionType {

	READ("permission.roleAttribute.read"), CREATE("permission.roleAttribute.create",
			READ), UPDATE("permission.roleAttribute.update", READ), DELETE(
			"permission.roleAttribute.delete", READ);

	private final String val;

	private PermissionType[] implies;

	private RoleAttributePermission(final String val, PermissionType... implies) {
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
