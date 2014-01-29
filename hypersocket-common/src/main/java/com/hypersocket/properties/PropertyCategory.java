/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.List;

public class PropertyCategory {

	String categoryKey;
	String bundle;
	int weight;
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
}
