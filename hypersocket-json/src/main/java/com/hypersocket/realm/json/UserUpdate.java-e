/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.properties.json.PropertyItem;

@XmlRootElement(name="user")
public class UserUpdate {

	String name;
	Long id;
	Long[] groups;
	PropertyItem[] properties;
	
	public UserUpdate() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PropertyItem[] getProperties() {
		return properties;
	}

	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public void setGroups(Long[] groups) {
		this.groups = groups;
	}
	
	public Long[] getGroups() {
		return groups;
	}

	
	
}
