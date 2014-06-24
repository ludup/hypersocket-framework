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
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.hypersocket.permissions.Role;
import com.hypersocket.resource.Resource;

@Entity
@XmlRootElement(name = "principal")
public abstract class Principal extends Resource {

	@ManyToOne
	@JoinColumn(name = "realm_id")
	protected Realm realm;

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade({ CascadeType.SAVE_UPDATE })
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	Set<Role> roles = new HashSet<Role>();

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	@XmlTransient
	@JsonIgnore
	public Realm getRealm() {
		return realm;
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
