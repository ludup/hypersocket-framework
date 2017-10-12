/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	@Fetch(FetchMode.SELECT)
	Set<Role> roles = new HashSet<Role>();
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.EAGER, mappedBy="principal")
	Set<PrincipalSuspension> suspensions;
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name="principal_links", joinColumns = { @JoinColumn(name = "principals_resource_id") }, inverseJoinColumns = { @JoinColumn(name = "linkedPrincipals_resource_id") })
	Set<Principal> linkedPrincipals;
	
	@ManyToOne
	@JoinColumn(name="parent_principal")
	Principal parentPrincipal;

	@Column(name="principal_type")
	PrincipalType principalType = getType();
	
	@Column(name="primary_email")
	String primaryEmail;
	
	@Column(name="ou", length=1024)
	String organizationalUnit;
	
	@JsonIgnore
	public Realm getRealm() {
		return super.getRealm();
	}
	
	public String getRealmName() {
		return super.getRealm().getName();
	}
	
	public String getPrimaryEmail() {
		return primaryEmail;
	}

	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}
	
	public abstract String getEmail();
	
	public abstract Date getExpires();
	
	public abstract PrincipalStatus getPrincipalStatus();
	
	public boolean isPrimaryAccount() {
		return super.getRealm().getOwner()==null;
	}

	@Transient
	public abstract PrincipalType getType();

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

	public boolean isSuspended() {
		for(PrincipalSuspension s : suspensions) {
			if(s.isActive()) {
				return true;
			}
		}
		return false;
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
	
	public String getOrganizationalUnit() {
		return organizationalUnit;
	}

	public void setOrganizationalUnit(String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
	}

	public abstract String getRealmModule();

}
