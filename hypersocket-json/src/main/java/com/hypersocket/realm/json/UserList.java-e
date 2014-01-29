/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.realm.Principal;

@XmlRootElement(name="users")
public class UserList {

	List<Principal> users;
	
	public UserList() {
		
	}
	
	public UserList(List<Principal> users) {
		this.users = users;
	}

	@XmlElement(name="user")
	public List<Principal> getUsers() {
		return users;
	}
	
	public void setUsers(List<Principal> users) {
		this.users = users;
	}
}
