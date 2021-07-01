/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.GroupPrincipal;
import com.hypersocket.realm.PrincipalStatus;

@Entity
@Table(name = "local_groups")
@XmlRootElement(name="group")
public class LocalGroup extends GroupPrincipal<LocalUser, LocalGroup> {

	private static final long serialVersionUID = 8447795179729315608L;

	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "local_user_groups", joinColumns={@JoinColumn(name="guid")}, inverseJoinColumns={@JoinColumn(name="uuid")})
	private Set<LocalUser> users = new HashSet<LocalUser>();
	
	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "local_group_groups", joinColumns={@JoinColumn(name="guid")}, inverseJoinColumns={@JoinColumn(name="gguid")})
	private Set<LocalGroup> groups = new HashSet<LocalGroup>();
	
	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "local_group_groups", joinColumns={@JoinColumn(name="gguid")}, inverseJoinColumns={@JoinColumn(name="guid")})
	private Set<LocalGroup> parents = new HashSet<LocalGroup>();
	
	@Column(name="posix_id")
	private Integer posixId;
	
	public int getPosixId() {
		return posixId == null ? 0 : posixId;
	}

	public void setPosixId(Integer posixId) {
		this.posixId = posixId;
	}

	@Override
	public PrincipalStatus getPrincipalStatus() {
		return PrincipalStatus.ENABLED;
	}
	
	@Override
	public String getOtherName() {
		return getPrincipalName();
	}
	
	public String getIcon() {
		return "fa-database";
	}
	
	@Override
	@JsonIgnore
	public Set<LocalUser> getUsers() {
		return users;
	}
	
	@Override
	@JsonIgnore
	public Set<LocalGroup> getGroups() {
		return groups;
	}
	
	@Override
	@JsonIgnore
	public Set<LocalGroup> getParents() {
		return parents;
	}

	@Override
	public String getRealmModule() {
		return LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY;
	}
}
