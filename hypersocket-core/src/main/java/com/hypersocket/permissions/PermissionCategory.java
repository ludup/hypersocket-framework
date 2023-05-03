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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "permission_category", uniqueConstraints = {@UniqueConstraint(columnNames={"resource_bundle", "resource_key"})})
public class PermissionCategory extends AbstractEntity<Long> {

	private static final long serialVersionUID = -9024441856361180370L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id")
	private Long id;

	@Column(name="resource_bundle", nullable=false, length = 191)	
	private String resourceBundle;
	
	@Column(name="resource_key", nullable=false, length = 191)	
	private String resourceKey;

	@OneToMany(mappedBy="category", fetch=FetchType.EAGER)
	@Cascade(CascadeType.DELETE)
	private Set<Permission> permissions = new HashSet<Permission>();

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
	
	public String getResourceBundle() {
		return resourceBundle;
	}
	
	void setResourceBundle(String resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
	
	public Set<Permission> getPermissions() {
		return permissions;
	}
}
