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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.hypersocket.permissions.Role;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="assignable_resources")
public abstract class AssignableResource extends RealmResource {

	@ManyToMany(fetch=FetchType.EAGER)
	@Cascade({CascadeType.ALL})
	@JoinTable(name = "resource_roles", joinColumns={@JoinColumn(name="resource_id")}, 
			inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> roles = new HashSet<Role>();

	public AssignableResource() {
	}
	
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
}
