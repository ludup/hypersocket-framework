/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.migration.annotation.AllowNameOnlyLookUp;
import com.hypersocket.permissions.Role;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "auth_schemes")
@AllowNameOnlyLookUp
public class AuthenticationScheme extends RealmResource {

	private static final long serialVersionUID = 96922791807675582L;

	@Column(name = "resourceKey")
	String resourceKey;

	@Column(name = "priority")
	Integer priority;
	
	@Column(name = "max_modules", nullable=true)
	Integer maximumModules;

	@Column(name = "type", nullable=true)
	AuthenticationModuleType type;
	
	@Column(name = "allowed_modules")
	String allowedModules;
	
	@Column(name = "last_button_resource_key")
	String lastButtonResourceKey;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "scheme_allowed_roles", joinColumns={@JoinColumn(name="resource_id")}, 
			inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> allowedRoles = new HashSet<Role>();
	
	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "scheme_denied_roles", joinColumns={@JoinColumn(name="resource_id")}, 
			inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> deniedRoles = new HashSet<Role>();
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="scheme") 
	Set<AuthenticationModule> modules;
	
	@Column
	String deniedRoleError;
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String name) {
		this.resourceKey = name;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getMaximumModules() {
		return maximumModules == null ? 10 : maximumModules;
	}

	public void setMaximumModules(Integer maximumModules) {
		if(maximumModules==null) {
			this.maximumModules = 10;
		} else {
			this.maximumModules = maximumModules;
		}
	}

	public AuthenticationModuleType getType() {
		return type==null ? AuthenticationModuleType.HTML : type;
	}

	public void setType(AuthenticationModuleType type) {
		if(type==null) {
			this.type = AuthenticationModuleType.HTML;
		} else {
			this.type = type;
		}
	}

	public String getAllowedModules() {
		return allowedModules;
	}
	
	public void setAllowedModules(String allowedModules) {
		this.allowedModules = allowedModules;
	}

	public String getLastButtonResourceKey() {
		return lastButtonResourceKey == null ? "text.logon" : lastButtonResourceKey;
	}
	
	public void setLastButtonResourceKey(String lastButtonResourceKey) {
		this.lastButtonResourceKey = lastButtonResourceKey;
	}
	
	@JsonIgnore
	public Set<Role> getAllowedRoles() {
		return allowedRoles;
	}

	@JsonIgnore
	public Set<Role> getDeniedRoles() {
		return deniedRoles;
	}

	@JsonIgnore
	public String getDeniedRoleError() {
		return deniedRoleError;
	}

	public void setDeniedRoleError(String deniedRoleError) {
		this.deniedRoleError = deniedRoleError;
	}
	

}
