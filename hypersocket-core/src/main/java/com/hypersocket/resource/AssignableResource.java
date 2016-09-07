/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.permissions.Role;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="assignable_resources")
public class AssignableResource extends RealmResource {

	private static final long serialVersionUID = 7293251973484666341L;
	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "resource_roles", joinColumns={@JoinColumn(name="resource_id")}, 
			inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> roles = new HashSet<Role>();

	@Transient
	Set<Role> assignedRoles;
	
	@Transient
	Set<Role> unassignedRoles;
	
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
	
	
	
}
