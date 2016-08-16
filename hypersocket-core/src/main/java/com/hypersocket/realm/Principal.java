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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

	private static final long serialVersionUID = -2289438956153713201L;

	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE })
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	@Fetch(FetchMode.SELECT)
	Set<Role> roles = new HashSet<Role>();
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy="principal")
	Set<PrincipalSuspension> suspensions;
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name="principal_links")
	Set<Principal> linkedPrincipals;
	
	@OneToOne
	@JoinColumn(name="parent_principal")
	Principal parentPrincipal;

	@Column(name="principal_type")
	PrincipalType principalType;
	
	@JsonIgnore
	public Realm getRealm() {
		return super.getRealm();
	}
	
	public boolean isPrimaryAccount() {
		return super.getRealm().getOwner()==null;
	}

	public abstract PrincipalType getType();

	public abstract String getIcon();
	
	public final PrincipalType getPrincipalType() {
		return principalType;
	}
	
	public void setPrincipalType(PrincipalType type) {
		this.principalType = type;
	}

	public abstract String getPrincipalDescription();
	
	@XmlElement(name = "principalName")
	public String getPrincipalName() {
		return getName();
	}
	
	public boolean isLinked() {
		return parentPrincipal!=null;
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

	@JsonIgnore
	public Set<Principal> getLinkedPrincipals() {
		return linkedPrincipals;
	}

	public void setLinkedPrincipals(Set<Principal> linkedPrincipals) {
		this.linkedPrincipals = linkedPrincipals;
	}

	public Principal getParentPrincipal() {
		return parentPrincipal;
	}

	public void setParentPrincipal(Principal parentPrincipal) {
		this.parentPrincipal = parentPrincipal;
	}
	
	public abstract String getRealmModule();
}
