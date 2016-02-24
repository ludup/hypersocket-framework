/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;

@Entity
@Table(name = "local_groups")
@XmlRootElement(name="group")
public class LocalGroup extends Principal {

	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "local_user_groups", joinColumns={@JoinColumn(name="guid")}, inverseJoinColumns={@JoinColumn(name="uuid")})
	private Set<LocalUser> users = new HashSet<LocalUser>();
	
	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "local_group_groups", joinColumns={@JoinColumn(name="guid")}, inverseJoinColumns={@JoinColumn(name="gguid")})
	private Set<LocalGroup> groups = new HashSet<LocalGroup>();
	
	@Override
	public PrincipalType getType() {
		return PrincipalType.GROUP;
	}
	
	public String getPrincipalDescription() {
		return getName();
	}
	
	@JsonIgnore
	public Set<LocalUser> getUsers() {
		return users;
	}
	
	@JsonIgnore
	public Set<LocalGroup> getGroups() {
		return groups;
	}


}
