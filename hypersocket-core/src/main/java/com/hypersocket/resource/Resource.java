/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="resources")
public abstract class Resource extends AbstractResource {
	
	@Column(name="name", nullable=false)
	String name;
	
	@Column(name="hidden")
	boolean hidden;
	
	@Column(name="resource_category", nullable=true)
	String resourceCategory;
	
	@Column(name="system", nullable=false)
	boolean system = false;
	
	@Transient
	String oldName;
	
	@Transient
	Map<String,String> properties;
	
	public boolean isSystem() {
		return system;
	}

	public Long getId() {
		/**
		 * Because AbstractResource uses generic super class with variable id type, if you
		 * query the return type you will get Object rather than Long. We need to query 
		 * this in Hibernate search configuration so we create a concrete method here
		 * that returns the correct Long id type - LDP
		 */
		return super.getId();
	}
	
	public void setSystem(boolean system) {
		this.system = system;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.oldName = this.name;
		this.name = StringUtils.trimWhitespace(name);
	}

	public boolean hasNameChanged() {
		return oldName!=null && !oldName.equals(name);
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public String getResourceCategory() {
		return resourceCategory;
	}
	
	public void setResourceCategory(String resourceCategory) {
		this.resourceCategory = resourceCategory;
	}

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		super.doHashCodeOnKeys(builder);
		builder.append(name);
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		super.doEqualsOnKeys(builder, obj);
		builder.append(name, ((Resource)obj).getName());
	}
	
	@JsonIgnore
	public String getOldName() {
		return oldName;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	
}
