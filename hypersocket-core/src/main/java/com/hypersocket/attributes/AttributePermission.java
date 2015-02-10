/*******************************************************************************
 * Copyright (c) 2013-2015 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.attributes;

import com.hypersocket.permissions.PermissionType;

public enum AttributePermission implements PermissionType {

	READ("permission.attribute.read"), CREATE("permission.attribute.create",
			READ), UPDATE("permission.attribute.update", READ), DELETE(
			"permission.attribute.delete", READ);

	private final String val;

	private PermissionType[] implies;

	private AttributePermission(final String val, PermissionType... implies) {
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
