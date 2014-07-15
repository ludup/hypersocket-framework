/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.hypersocket.permissions.Role;
import com.hypersocket.resource.RealmResource;

@Entity
@XmlRootElement(name = "principal")
public abstract class Principal extends RealmResource {

	

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.PERSIST)
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	@Fetch(FetchMode.SELECT)
	Set<Role> roles = new HashSet<Role>();

	@JsonIgnore
	public Realm getRealm() {
		return super.getRealm();
	}

	@JsonIgnore
	public Set<Role> getRoles() {
		return roles;
	}

	public abstract PrincipalType getType();

	public abstract String getAddress(MediaType type) throws MediaNotFoundException;
	
	@XmlElement(name = "principalName")
	public String getPrincipalName() {
		return getName();
	}

	public abstract String getPrincipalDesc();

}
