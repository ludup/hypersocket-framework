/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

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

}
