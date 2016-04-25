/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.automation;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.triggers.TriggerResourcePermission;


public enum AutomationResourcePermission implements PermissionType {
	
	READ("read", TriggerResourcePermission.READ),
	CREATE("create", READ),
	UPDATE("update", READ),
	DELETE("delete", READ);
	
	private final String val;
	
	private final static String name = "automation";
	
	private PermissionType[] implies;
	
	private AutomationResourcePermission(final String val, PermissionType... implies) {
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
