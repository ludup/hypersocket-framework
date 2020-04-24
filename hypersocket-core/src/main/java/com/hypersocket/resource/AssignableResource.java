/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "assignable_resources")
public abstract class AssignableResource extends RealmResource {

	private static final long serialVersionUID = 7293251973484666341L;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "resource_roles", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Role> roles = new HashSet<Role>();

	@Transient
	private Set<Role> assignedRoles;

	@Transient
	private Set<Role> unassignedRoles;

	@Column(name = "personal")
	private Boolean personal;
	
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "assignable_resources_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	protected Realm realm;

	@Override
	protected Realm doGetRealm() {
		return realm;
	}

	@Override
	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	public AssignableResource() {
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@JsonIgnore
	public Set<Role> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(Set<Role> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	@JsonIgnore
	public Set<Role> getUnassignedRoles() {
		return unassignedRoles;
	}

	public void setUnassignedRoles(Set<Role> unassignedRoles) {
		this.unassignedRoles = unassignedRoles;
	}

	public Boolean getPersonal() {
		return personal == null ? Boolean.FALSE : personal;
	}

	public void setPersonal(Boolean personal) {
		this.personal = personal;
	}
}
