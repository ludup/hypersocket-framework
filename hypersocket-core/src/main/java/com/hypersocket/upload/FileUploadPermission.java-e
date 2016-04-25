/*******************************************************************************
 * Copyright (c) 2013-2015 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upload;

import com.hypersocket.permissions.PermissionType;

/**
 * This is intentionally not used but currently requires in resource framework.
 * @author lee
 *
 */
public enum FileUploadPermission implements PermissionType {

	READ("permission.fileUpload.read"), 
	CREATE("permission.fileUpload.create", READ), 
	DELETE("permission.fileUpload.delete", READ);

	private final String val;

	private PermissionType[] implies;

	private FileUploadPermission(final String val, PermissionType... implies) {
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
