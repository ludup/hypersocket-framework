/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.UserPrincipal;

@Entity
@Table(name = "local_users")
@XmlRootElement(name="user")
public class LocalUser extends Principal implements UserPrincipal, Serializable {
	
	private static final long serialVersionUID = 4490274955679793663L;

	@Column(name="type", nullable=false)
	PrincipalType type = PrincipalType.USER;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	@JoinTable(name = "local_user_groups", joinColumns={@JoinColumn(name="uuid")}, inverseJoinColumns={@JoinColumn(name="guid")})
	private Set<LocalGroup> groups = new HashSet<LocalGroup>();

	@OneToOne(mappedBy="user", optional=true)
	@Cascade({CascadeType.DELETE})
	LocalUserCredentials credentials;
	
	@JsonIgnore
	public Set<LocalGroup> getGroups() {
		return groups;
	}

	@Override
	public PrincipalType getType() {
		return type;
	}
	
	public void setType(PrincipalType type) {
		this.type = type;
	}	
	
	@JsonIgnore
	public Set<Principal> getAssociatedPrincipals() {
		return new HashSet<Principal>(groups);
	}
	
}
