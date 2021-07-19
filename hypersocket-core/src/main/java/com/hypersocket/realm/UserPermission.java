/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import com.hypersocket.permissions.PermissionType;

public enum UserPermission implements PermissionType {
	
	READ("user.read"),
	CREATE("user.create", READ),
	UPDATE("user.update", READ),
	DELETE("user.delete", READ),
	IMPERSONATE("user.impersonate", READ),
	LOCK("user.lock", READ),
	UNLOCK("user.unlock", READ),
	/* NOTE: @ludup, this is temporary. I did not merge all 
	 * of the ticket that introduced this due to conflicts apparently
	 * related to delegation, this is just to get a build.
	 */
	RESET_CREDENTIALS("user.reset", READ);
	
	private final String val;
	
	private PermissionType[] implies;
	
	private UserPermission(final String val, PermissionType... implies) {
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
