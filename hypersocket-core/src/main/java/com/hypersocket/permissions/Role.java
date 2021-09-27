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
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "roles")
@XmlRootElement(name = "role")
public class Role extends RealmResource {

	private static final long serialVersionUID = -9007753723140808832L;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
//	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@JoinTable(name = "role_permissions", joinColumns = { @JoinColumn(name = "role_id") }, inverseJoinColumns = {
			@JoinColumn(name = "permission_id") })
	private Set<Permission> permissions = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
//	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@JoinTable(foreignKey = @ForeignKey(name = "role_principals_cascade_1"), inverseForeignKey = @ForeignKey(name = "role_principals_cascade_2"), name = "role_principals", joinColumns = { @JoinColumn(name = "role_id") }, inverseJoinColumns = {
			@JoinColumn(name = "principal_id") })
	/* BUG: https://hibernate.atlassian.net/browse/HHH-11856
	 * 
	 * This is a total pain
	 **/
	private Set<Principal> principals = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "role_realms", joinColumns = { @JoinColumn(name = "role_id") }, inverseJoinColumns = {
			@JoinColumn(name = "principal_id") })
	private Set<Realm> realms = new HashSet<>();

	@Column(name = "all_users", nullable = false)
	private boolean allUsers;

	@Column(name = "all_permissions", nullable = false)
	private boolean allPermissions;

	@Column(name = "all_realms", nullable = false)
	private Boolean allRealms = Boolean.FALSE;

	@Column(name = "personal_role", nullable = true)
	private Boolean personalRole = Boolean.valueOf(false);

	@Column(name = "role_type")
	private RoleType type;

	@Column(name = "principal_name")
	private String principalName;
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "roles_cascade_1"))
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
		return personalRole != null && personalRole.booleanValue();
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
		return principalName == null ? isPersonalRole() ? StringUtils.substringAfter(getName(), "/") : getName()
				: principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	@JsonIgnore
	public Set<Realm> getPermissionRealms() {
		/**
		 * Protection against a role having no realms. Default it to the roles own
		 * realm.
		 */
		if (Objects.isNull(realms) || realms.isEmpty()) {
			return new HashSet<Realm>(Arrays.asList(getRealm()));
		}
		return realms;
	}

	public void setPermissionRealms(Set<Realm> realms) {
		this.realms = realms;
	}
	
	public String getSystemName() {
		return String.format("%s/%s", getRealm().getName(), getName());
	}
}
