/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.hypersocket.permissions.Role;
import com.hypersocket.resource.Resource;

@Entity
@Table(name = "realms")
@XmlRootElement(name = "realm")
public class Realm extends Resource {

	private static final long serialVersionUID = -5087610813626724784L;

	@Column(name = "default_realm")
	private boolean defaultRealm = false;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "owner_id")
	private Long owner;

	@OneToOne(optional=true)
	private Realm parent;
	
	@Column(name="public_realm")
	private Boolean publicRealm;
	
	@ManyToMany(/* fetch=FetchType.EAGER */)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "role_realms", joinColumns={@JoinColumn(name="principal_id")}, inverseJoinColumns={@JoinColumn(name="role_id")})
	private Set<Role> roles = new HashSet<>();
	
	public boolean isDefaultRealm() {
		return defaultRealm;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public void setDefaultRealm(boolean defaultRealm) {
		this.defaultRealm = defaultRealm;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(getName());
	}

	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		Realm r = (Realm) obj;
		builder.append(getName(), r.getName());
	}

	public boolean hasOwner() {
		return Objects.nonNull(owner);
	}
	
	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}

	public boolean isPrimaryRealm() {
		return owner==null && parent==null;
	}

	public Realm getParent() {
		return parent;
	}
	
	public boolean hasParent() {
		return parent!=null;
	}

	public void setParent(Realm parent) {
		this.parent = parent;
	}

	public Boolean getPublicRealm() {
		return publicRealm==null ? isPrimaryRealm() : publicRealm;
	}

	public void setPublicRealm(Boolean publicRealm) {
		this.publicRealm = publicRealm;
	}
	
	
}
