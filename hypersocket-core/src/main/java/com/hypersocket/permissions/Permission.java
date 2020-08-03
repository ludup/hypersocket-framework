/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "permissions")
public class Permission extends AbstractEntity<Long> {

	private static final long serialVersionUID = 820019210375733854L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id")
	private Long id;
	
	@Column(name="resource_key", nullable=false, unique=true)
	private String resourceKey;
	
	@ManyToOne
	@JoinColumn(name="category_id", nullable=false)
	private PermissionCategory category;

	@Column(name="hidden")
	private boolean hidden;
	
	@Column(name="system_permission")
	private boolean system;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "role_permissions", joinColumns={@JoinColumn(name="permission_id")}, inverseJoinColumns={@JoinColumn(name="role_id")})
	private Set<Role> roles = new HashSet<Role>();
	
	public Long getId() {
		return id;
	}
	
	void setId(Long id) {
		this.id = id;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	void setResourceKey(String name) {
		this.resourceKey = name;
	}
	
	@JsonIgnore
	@XmlTransient
	public PermissionCategory getCategory() {
		return category;
	}
	
	public void setCategory(PermissionCategory category) {
		this.category = category;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
	
	public boolean isSystem() {
		return system;
	}
	
}
