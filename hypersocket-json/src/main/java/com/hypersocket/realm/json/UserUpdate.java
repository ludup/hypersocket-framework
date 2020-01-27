/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import com.hypersocket.json.PropertyItem;

public class UserUpdate {

	private String name;
	private Long id;
	private String[] groups;
	
	private String password;
	private boolean forceChange;
	private PropertyItem[] properties;

	public PropertyItem[] getProperties() {
		return properties;
	}

	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}
	
	public UserUpdate() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public void setGroups(String[] groups) {
		this.groups = groups;
	}
	
	public String[] getGroups() {
		return groups;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isForceChange() {
		return forceChange;
	}

	public void setForceChange(boolean forceChange) {
		this.forceChange = forceChange;
	}

	
	
}
