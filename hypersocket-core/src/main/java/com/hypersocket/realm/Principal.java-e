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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.permissions.Role;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "principals")
public abstract class Principal extends RealmResource {

	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE })
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	@Fetch(FetchMode.SELECT)
	Set<Role> roles = new HashSet<Role>();
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy="principal")
	Set<PrincipalSuspension> suspensions;
	
	@JsonIgnore
	public Realm getRealm() {
		return super.getRealm();
	}

	public abstract PrincipalType getType();

	public abstract String getPrincipalDescription();
	
	@XmlElement(name = "principalName")
	public String getPrincipalName() {
		return getName();
	}
	
	@JsonIgnore
	public Set<PrincipalSuspension> getSuspensions() {
		return suspensions;
	}

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(getRealm());
		builder.append(getName());
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		Principal r = (Principal) obj;
		builder.append(getRealm(), r.getRealm());
		builder.append(getName(), r.getName());
	}
}
