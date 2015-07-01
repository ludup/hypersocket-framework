/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.interfaceState;

import com.hypersocket.permissions.PermissionType;

public enum UserInterfaceStatePermission implements PermissionType {
	
	READ("userInterfaceState.read"),
	CREATE("userInterfaceState.create", READ),
	UPDATE("userInterfaceState.update", READ),
	DELETE("userInterfaceState.delete", READ);
	
	private final String val;
	
	private PermissionType[] implies;
	
	private UserInterfaceStatePermission(final String val, PermissionType... implies) {
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
