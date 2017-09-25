/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalStatus;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.UserPrincipal;

@Entity
@Table(name = "local_users")
@XmlRootElement(name="user")
public class LocalUser extends UserPrincipal implements Serializable {
	
	private static final long serialVersionUID = 4490274955679793663L;

	@Column(name="type", nullable=false)
	PrincipalType type = PrincipalType.USER;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	@JoinTable(name = "local_user_groups", joinColumns={@JoinColumn(name="uuid")}, inverseJoinColumns={@JoinColumn(name="guid")})
	private Set<LocalGroup> groups = new HashSet<>();

	@OneToOne(mappedBy="user", optional=true)
	@Cascade({CascadeType.DELETE})
	LocalUserCredentials credentials;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date lastSignOn;
	
	@Column(name="fullname")
	String fullname;
	
	@Column(name="email")
	String email;

	@Column(name="secondary_email")
	String secondaryEmail;
	
	@Column(name="mobile")
	String mobile;
	
	@Column(name="realm_category")
	String realmCategory;
	
	@Column(name="user_expires")
	@Temporal(TemporalType.DATE)
	Date expires;
	
	public String getIcon() {
		return "fa-database";
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	@JsonIgnore
	public Set<LocalGroup> getGroups() {
		return groups;
	}

	@Override
	public PrincipalType getType() {
		return type;
	}
	
	@Override
	public PrincipalStatus getPrincipalStatus() {
		if(getExpires()!=null) {
			if(new Date().before(getExpires())) {
				return PrincipalStatus.EXPIRED;
			}
		}
		return PrincipalStatus.ENABLED;
	}
	@Override
	public String getPrincipalDescription() {
		return StringUtils.isBlank(getFullname()) ? getName() : getFullname();
	}
	
	public void setType(PrincipalType type) {
		this.type = type;
	}	
	
	@JsonIgnore
	public Set<Principal> getAssociatedPrincipals() {
		return new HashSet<Principal>(groups);
	}

	@Override
	public Date getLastPasswordChange() {
		if(credentials!=null) {
			return credentials.getModifiedDate();
		}
		return null;
	}

	@Override
	public Date getLastSignOn() {
		return lastSignOn;
	}

	public void setLastSignOn(Date lastSignOn) {
		this.lastSignOn = lastSignOn;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		setPrimaryEmail(email);
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@Override
	public String getRealmModule() {
		return LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY;
	}

	public String getRealmCategory() {
		return realmCategory;
	}

	public void setRealmCategory(String realmCategory) {
		this.realmCategory = realmCategory;
	}

	@Override
	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	@Override
	public boolean isPasswordExpiring() {
		return false;
	}

	@Override
	public boolean isPasswordChangeRequired() {
		if(credentials!=null) {
			return credentials.isPasswordChangeRequired();
		}
		return true;
	}

	@Override
	public boolean isPasswordChangeAllowed() {
		return true;
	}

	@Override
	public Date getPasswordExpiry() {
		return null;
	}
	
	@Override
	public String getSecondaryEmail() {
		return secondaryEmail;
	}
	
	public void setSecondaryEmail(String secondaryEmail) {
		this.secondaryEmail = secondaryEmail;
	}
}
