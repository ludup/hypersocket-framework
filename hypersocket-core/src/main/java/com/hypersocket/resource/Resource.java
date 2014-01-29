/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class Resource extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	Long id;
	
	@Column(name="name", nullable=false)
	String name;
	
	@Column(name="hidden")
	boolean hidden;
	
	@Column(name="resource_category", nullable=true)
	String resourceCategory;
	
	@OneToMany(mappedBy = "resource", fetch = FetchType.EAGER)
	protected Set<DatabaseProperty> properties;
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	@JsonIgnore
	public Map<String, DatabaseProperty> getProperties() {
		HashMap<String, DatabaseProperty> mappedProperties = new HashMap<String, DatabaseProperty>();
		if (properties != null) {
			for (DatabaseProperty p : properties) {
				mappedProperties.put(p.getResourceKey(), p);
			}
		}

		return Collections.unmodifiableMap(mappedProperties);
	}

	public String getProperty(String resourceKey) {

		Map<String, DatabaseProperty> properties = getProperties();

		if (properties != null && properties.containsKey(resourceKey)) {
			return properties.get(resourceKey).getValue();
		} else {
			return "";
		}
	}
	
	public int getPropertyInt(String resourceKey) {
		return Integer.parseInt(getProperty(resourceKey));
	}
}
