/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

public enum SystemPermission implements PermissionType {

	SYSTEM_ADMINISTRATION("system.administration"),
	SYSTEM("system.permission");
	
	private final String val;
	
	private SystemPermission(final String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}

	@Override
	public String getResourceKey() {
		return val;
	}
}
