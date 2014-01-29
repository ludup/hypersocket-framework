/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="group")
public class GroupUpdate {

	Long id;
	String name;
	Long[] users;
	
	public GroupUpdate() {
		
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

	public Long[] getUsers() {
		return users;
	}

	public void setUsers(Long[] users) {
		this.users = users;
	}

}
