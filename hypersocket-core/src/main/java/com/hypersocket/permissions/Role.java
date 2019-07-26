/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "roles")
@XmlRootElement(name="role")
public class Role extends RealmResource {

	private static final long serialVersionUID = -9007753723140808832L;

	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
//	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@JoinTable(name = "role_permissions", 
		joinColumns = {@JoinColumn(name="role_id")}, 
		inverseJoinColumns = {@JoinColumn(name="permission_id")})
	private Set<Permission> permissions = new HashSet<>();

	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
//	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@JoinTable(name = "role_principals", joinColumns={@JoinColumn(name="role_id")}, inverseJoinColumns={@JoinColumn(name="principal_id")})
	Set<Principal> principals = new HashSet<>();
	
	@ManyToMany(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "role_realms", joinColumns={@JoinColumn(name="role_id")}, inverseJoinColumns={@JoinColumn(name="principal_id")})
	Set<Realm> realms = new HashSet<>();
	
	@Column(name="all_users", nullable=false)
	boolean allUsers;
	
	@Column(name="all_permissions", nullable=false)
	boolean allPermissions;

	@Column(name="all_realms", nullable=false)
	Boolean allRealms = Boolean.FALSE;
	
	@Column(name="personal_role", nullable=true)
	Boolean personalRole = new Boolean(false);
	
	@Column(name="role_type")
	RoleType type;
	
	@Column(name="principal_name")
	String principalName;
	
	@JsonIgnore
	public Set<Permission> getPermissions() {
		return permissions;
	}
	
	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}	
	
	@JsonIgnore
	public Set<Principal> getPrincipals() {
		return principals;
	}

	public void setPrincipals(Set<Principal> principals) {
		this.principals = principals;
	}
	
	public boolean isAllUsers() {
		return allUsers;
	}
	
	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}
	
	public boolean isPersonalRole() {
		return personalRole!=null && personalRole.booleanValue();
	}
	
	public boolean isAllPermissions() {
		return allPermissions;
	}

	public void setAllPermissions(boolean allPermissions) {
		this.allPermissions = allPermissions;
	}

	public void setPersonalRole(Boolean personalRole) {
		this.personalRole = personalRole;
	}

	public boolean isAllRealms() {
		return allRealms == null ? Boolean.FALSE.booleanValue() : allRealms;
	}

	public void setAllRealms(boolean allRealms) {
		this.allRealms = allRealms;
	}

	public RoleType getType() {
		return type;
	}

	public void setType(RoleType type) {
		this.type = type;
	}

	public String getPrincipalName() {
		return principalName == null ? 
				isPersonalRole() ? StringUtils.substringAfter(getName(), "/") : getName()
			   : principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}
	
	@JsonIgnore
	public Set<Realm> getPermissionRealms() {
		/**
		 * Protection against a role having no realms. Default it to the roles own realm.
		 */
		if(Objects.isNull(realms) || realms.isEmpty()) {
			return new HashSet<Realm>(Arrays.asList(getRealm()));
		}
		return realms;
	}

	public void setPermissionRealms(Set<Realm> realms) {
		this.realms = realms;
	}
}
