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

import javax.persistence.Cacheable;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.permissions.Role;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "principals")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Principal extends RealmResource implements java.security.Principal {

	private static final long serialVersionUID = -2289438956153713201L;

	@SuppressWarnings("deprecation")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE_ORPHAN })
	@JoinTable(name = "role_principals", joinColumns = { @JoinColumn(name = "principal_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	@Fetch(FetchMode.SELECT)
	/*
	 * @OnDelete(action = OnDeleteAction.CASCADE)
	 * https://hibernate.atlassian.net/browse/HHH-5875 Have added constraints via Db
	 * script anyway
	 */
	private Set<Role> roles = new HashSet<Role>();

	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "principal")
	@JsonIgnore
	private Set<PrincipalSuspension> suspensions;

	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "principal_links", joinColumns = {
			@JoinColumn(name = "principals_resource_id") }, inverseJoinColumns = {
					@JoinColumn(name = "linkedPrincipals_resource_id") })
	private Set<Principal> linkedPrincipals;

	@ManyToOne
	@JoinColumn(name = "parent_principal", foreignKey = @ForeignKey(name = "principals_cascade_2"))
	@OnDelete(action = OnDeleteAction.NO_ACTION) // TODO no set null
	private Principal parentPrincipal;

	@Column(name = "principal_type")
	private PrincipalType principalType = getType();

	@Column(name = "primary_email")
	private String primaryEmail;

	@Column(name = "ou", length = 1024)
	private String organizationalUnit;
	
	private Boolean suspended;

	public Long getId() {
		return super.getId();
	}

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "principals_cascade_1"))
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

	public String getRealmName() {
		return super.getRealm().getName();
	}

	public String getPrimaryEmail() {
		return primaryEmail;
	}

	public String getPrincipalDescription() {
		return StringUtils.defaultString(getDescription(), getPrincipalName());
	}

	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}

	public abstract String getOtherName();
	
	public abstract String getEmail();

	public abstract Date getExpires();

	public abstract PrincipalStatus getPrincipalStatus();

	public boolean isLocallyDeleted() {
		return false;
	}

	public boolean isPrimaryAccount() {
		return super.getRealm().getOwner() == null;
	}

	@Transient
	public abstract PrincipalType getType();

	public final PrincipalType getPrincipalType() {
		return principalType;
	}

	public void setPrincipalType(PrincipalType type) {
		this.principalType = type;
	}

	public abstract String getDescription();

	@XmlElement(name = "principalName")
	public String getPrincipalName() {
		return getName();
	}

	public boolean isLinked() {
		return parentPrincipal != null;
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

	public String getOrganizationalUnit() {
		return organizationalUnit;
	}

	public void setOrganizationalUnit(String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
	}

	public abstract String getRealmModule();

	public void setLocallyDeleted(boolean locallyDeleted) {
		throw new UnsupportedOperationException();
	}

	public Boolean isSuspended() {
		return suspended == null ? Boolean.FALSE : suspended;
	}

	public void setSuspended(Boolean suspended) {
		this.suspended = suspended;
	}
	
	@JsonIgnore
	public boolean isFake() {
		return principalType == PrincipalType.FAKE;
	}

	@Override
	public String toString() {
		return "Principal [primaryEmail=" + primaryEmail + ", organizationalUnit=" + organizationalUnit
				+ ", isLocallyDeleted()=" + isLocallyDeleted() + ", getPrincipalType()=" + getPrincipalType()
				+ ", getDescription()=" + getDescription() + ", getPrincipalName()=" + getPrincipalName()
				+ ", isSuspended()=" + isSuspended() + ", isSystem()=" + isSystem() + ", isHidden()=" + isHidden()
				+ ", isDeleted()=" + isDeleted() + "]";
	}

}
