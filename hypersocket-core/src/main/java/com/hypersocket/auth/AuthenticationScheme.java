/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "auth_schemes")
public class AuthenticationScheme extends RealmResource {

	private static final long serialVersionUID = 96922791807675582L;

	@Column(name = "resourceKey")
	private String resourceKey;

	@Column(name = "priority")
	private Integer priority;

	@Column(name = "max_modules", nullable = true)
	private Integer maximumModules;

	@Column(name = "type", nullable = true)
	private AuthenticationModuleType type;

	@Column(name = "allowed_modules")
	private String allowedModules;

	@Column(name = "last_button_resource_key")
	private String lastButtonResourceKey;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "scheme_allowed_roles", joinColumns = {
			@JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	private Set<Role> allowedRoles = new HashSet<Role>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "scheme_denied_roles", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Role> deniedRoles = new HashSet<Role>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "scheme", cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Set<AuthenticationModule> modules;

	@Column
	private String deniedRoleError;

	@Column
	private Boolean supportsHomeRedirect;

	@Column(name="scheme_2fa")
	private Boolean scheme2fa;
	
	@Column(name="authenticator_2fa")
	private String authenticator2fa;
	
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "auth_schemes_cascade_1"))
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

	public String getResourceKey() {
		return resourceKey;
	}

	public Set<AuthenticationModule> getModules() {
		return modules;
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
		if (maximumModules == null) {
			this.maximumModules = 10;
		} else {
			this.maximumModules = maximumModules;
		}
	}

	public AuthenticationModuleType getType() {
		return type == null ? AuthenticationModuleType.HTML : type;
	}

	public void setType(AuthenticationModuleType type) {
		if (type == null) {
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

	public void setAllowedRoles(Set<Role> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	public void setDeniedRoles(Set<Role> deniedRoles) {
		this.deniedRoles = deniedRoles;
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

	public boolean supportsHomeRedirect() {
		return supportsHomeRedirect == null ? Boolean.FALSE : supportsHomeRedirect;
	}

	public void setSupportsHomeRedirect(boolean supportsHomeRedirect) {
		this.supportsHomeRedirect = supportsHomeRedirect;
	}

	public Boolean getScheme2fa() {
		return scheme2fa == null ? Boolean.FALSE : scheme2fa;
	}

	public void setScheme2fa(Boolean scheme2fa) {
		this.scheme2fa = scheme2fa;
	}

	public String getAuthenticator2fa() {
		return authenticator2fa;
	}

	public void setAuthenticator2fa(String authenticator2fa) {
		this.authenticator2fa = authenticator2fa;
	}
	
	

}
