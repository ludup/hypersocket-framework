/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonConfiguration {
	private long id;
	private String bundle;
	private String categoryKey;
	private String categoryGroup;
	private JsonConfigurationTemplate[] templates;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBundle() {
		return bundle;
	}
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	public String getCategoryKey() {
		return categoryKey;
	}
	public void setCategoryKey(String categoryKey) {
		this.categoryKey = categoryKey;
	}
	public String getCategoryGroup() {
		return categoryGroup;
	}
	public void setCategoryGroup(String categoryGroup) {
		this.categoryGroup = categoryGroup;
	}
	public JsonConfigurationTemplate[] getTemplates() {
		return templates;
	}
	public void setTemplates(JsonConfigurationTemplate[] templates) {
		this.templates = templates;
	}
	
	
}
