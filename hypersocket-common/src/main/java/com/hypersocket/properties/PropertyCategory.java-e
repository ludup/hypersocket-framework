/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PropertyCategory implements Serializable {

	private static final long serialVersionUID = -9161050636516897409L;

	String categoryKey;
	String categoryGroup;
	String bundle;
	String displayMode;
	int weight;
	boolean userCreated;
	boolean systemOnly = false;
	boolean hidden;
	String filter = "default";
	
	List<AbstractPropertyTemplate> templates = new ArrayList<AbstractPropertyTemplate>();
	
	public PropertyCategory() {
		
	}
	
	public int getId() {
		return categoryKey.hashCode();
	}
	
	public String getCategoryKey() {
		return categoryKey;
	}

	public void setCategoryKey(String resourceKey) {
		this.categoryKey = resourceKey;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	
	public List<AbstractPropertyTemplate> getTemplates() {
		return templates;
	}

	public Integer getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public void setUserCreated(boolean userCreated) {
		this.userCreated = userCreated;
	}
	
	public boolean isUserCreated() {
		return userCreated;
	}

	public String getCategoryGroup() {
		return categoryGroup;
	}

	public void setCategoryGroup(String categoryGroup) {
		this.categoryGroup = categoryGroup;
	}

	public String getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}

	public boolean isSystemOnly() {
		return systemOnly;
	}

	public void setSystemOnly(boolean systemOnly) {
		this.systemOnly = systemOnly;
	}

	public void setFilter(String filter) {
		this.filter = !"".equals(filter) ? filter : "default";
	}
	
	public String getFilter() {
		return filter;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	
}
