/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "permission_category", uniqueConstraints = {@UniqueConstraint(columnNames={"resource_bundle", "resource_key"})})
@XmlRootElement(name="permissionCategory")
public class PermissionCategory extends AbstractEntity<Long> {

	@Id
	@GeneratedValue
	@Column(name="id")
	Long id;

	@Column(name="resource_bundle", nullable=false)	
	String resourceBundle;
	
	@Column(name="resource_key", nullable=false)	
	String resourceKey;

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
