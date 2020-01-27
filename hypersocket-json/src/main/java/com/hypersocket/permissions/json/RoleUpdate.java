/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.json.PropertyItem;

@XmlRootElement(name="role")
public class RoleUpdate {

	private Long id;
	private String name;
	private String[] users;
	private String[] groups;
	private Long[] permissions;
	private Long[] realms;
	private PropertyItem[] properties;
	private boolean allUsers;
	private boolean allPerms;
	private boolean allRealms;
	
	public RoleUpdate() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getUsers() {
		return users;
	}

	public void setUsers(String[] users) {
		this.users = users;
	}

	public String[] getGroups() {
		return groups;
	}

	public void setGroups(String[] groups) {
		this.groups = groups;
	}

	public Long[] getPermissions() {
		return permissions;
	}

	public void setPermissions(Long[] permissions) {
		this.permissions = permissions;
	}

	public PropertyItem[] getProperties() {
		return properties;
	}

	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}

	public Long[] getRealms() {
		return realms;
	}

	public void setRealms(Long[] realms) {
		this.realms = realms;
	}

	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}

	public boolean isAllPerms() {
		return allPerms;
	}

	public void setAllPerms(boolean allPerms) {
		this.allPerms = allPerms;
	}

	public boolean isAllRealms() {
		return allRealms;
	}

	public void setAllRealms(boolean allRealms) {
		this.allRealms = allRealms;
	}
	
	
}
