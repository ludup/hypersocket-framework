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
import java.util.Collection;
import java.util.List;

public class PropertyCategory implements Serializable {

	private static final long serialVersionUID = -9161050636516897409L;

	private String categoryKey;
	private String categoryGroup;
	private String categoryNamespace;
	private String bundle;
	private String displayMode;
	private int weight;
	private boolean userCreated;
	private boolean systemOnly = false;
	private boolean nonSystem = false;
	private boolean hidden;
	private String filter = "default";
	private String name = null;
	private String visibilityDependsOn = "";
	private String visibilityDependsValue = "";
	private List<AbstractPropertyTemplate> templates = new ArrayList<AbstractPropertyTemplate>();

	@SafeVarargs
	public static Collection<PropertyCategory> mergeCategories(Collection<PropertyCategory>... categoryLists) {
		int i = 0;
		for (Collection<PropertyCategory> l : categoryLists)
			i += l.size();
		List<PropertyCategory> all = new ArrayList<>(i);
		for (Collection<PropertyCategory> l : categoryLists) {
			if (l != null) {
				for (PropertyCategory c : l) {
					if (!all.contains(c)) {
						all.add(c);
					}
				}
			}
		}
		return all;
	}

	public static PropertyCategory findCategory(String key, Iterable<PropertyCategory> it) {
		for (PropertyCategory c : it) {
			if (c.getCategoryKey().equals(key)) {
				return c;
			}
		}
		return null;
	}

	public PropertyCategory() {
	}

	public PropertyCategory(PropertyCategory other) {

		setBundle(other.getBundle());
		setCategoryGroup(other.getCategoryGroup());
		setCategoryNamespace(other.getCategoryNamespace());
		setCategoryKey(other.getCategoryKey());
		setWeight(other.getWeight());
		setUserCreated(other.isUserCreated());
		setDisplayMode(other.getDisplayMode());
		setSystemOnly(other.isSystemOnly());
		setNonSystem(other.isNonSystem());
		setFilter(other.getFilter());
		setName(other.getName());
		setHidden(other.isHidden());
		setVisibilityDependsValue(other.getVisibilityDependsValue());
		setVisibilityDependsOn(other.getVisibilityDependsOn());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categoryKey == null) ? 0 : categoryKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyCategory other = (PropertyCategory) obj;
		if (categoryKey == null) {
			if (other.categoryKey != null)
				return false;
		} else if (!categoryKey.equals(other.categoryKey))
			return false;
		return true;
	}

	public int getId() {
		return categoryKey.hashCode();
	}

	public String getCategoryNamespace() {
		return categoryNamespace;
	}

	public void setCategoryNamespace(String categoryNamespace) {
		this.categoryNamespace = categoryNamespace;
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

	public boolean isNonSystem() {
		return nonSystem;
	}

	public void setNonSystem(boolean nonSystem) {
		this.nonSystem = nonSystem;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVisibilityDependsOn() {
		return visibilityDependsOn;
	}

	public void setVisibilityDependsOn(String visibilityDependsOn) {
		this.visibilityDependsOn = visibilityDependsOn;
	}

	public String getVisibilityDependsValue() {
		return visibilityDependsValue;
	}

	public void setVisibilityDependsValue(String visibilityDependsValue) {
		this.visibilityDependsValue = visibilityDependsValue;
	}

}
