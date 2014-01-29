/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions.json;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.permissions.Permission;


@XmlRootElement(name="permissions")
public class PermissionList {

	List<Permission> permissions;
	
	public PermissionList(List<Permission> permissions) {
		this.permissions = permissions;
	}
	
	@XmlElement(name="permission")
	public List<Permission> getPermissions() {
		return permissions;
	}
}
