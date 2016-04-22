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

@XmlRootElement(name="groups")
public class GroupList {

	List<Principal> groups;
	
	public GroupList() {
		
	}
	
	public GroupList(List<Principal> groups) {
		this.groups = groups;
	}

	@XmlElement(name="group")
	public List<Principal> getGroups() {
		return groups;
	}
	
	public void setGroups(List<Principal> groups) {
		this.groups = groups;
	}
}
