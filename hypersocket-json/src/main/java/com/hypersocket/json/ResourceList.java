/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value=Include.NON_NULL)
public class ResourceList<T> extends ResourceStatus<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2734622041269461985L;

	public static final int DEFAULT_MAXIMUM_RESOURCES = 1000;
	
	private Collection<T> resources;
	private Map<String,String> properties;
	
	public ResourceList() {
	}
	
	public ResourceList(Collection<T> resources) {
		this.resources = resources;
	}
	
	public ResourceList(boolean success, String message) {
		super(success, message);
	}
	
	public ResourceList(Map<String,String> properties, Collection<T> resources) {
		this.properties = properties;
		this.resources = resources;
	}
	
	public Collection<T> getResources() {
		return resources;
	}
	
	public void setResources(Collection<T> resources) {
		this.resources = resources;
	}
	
	public Map<String,String> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String,String> properties) {
		this.properties = properties;
	}
}

