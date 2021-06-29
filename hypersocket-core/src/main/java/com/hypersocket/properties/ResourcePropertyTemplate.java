/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import com.hypersocket.resource.SimpleResource;

public class ResourcePropertyTemplate extends AbstractPropertyTemplate {

	private SimpleResource resource;

	public ResourcePropertyTemplate(AbstractPropertyTemplate t, SimpleResource resource) {
		setResourceKey(t.getResourceKey());
		setDefaultValue(t.getDefaultValue());
		setMapping(t.getMapping());
		setWeight(t.getWeight());
		setCategory(t.getCategory());
		setHidden(t.isHidden());
		setDisplayMode(t.getDisplayMode());
		setReadOnly(t.isReadOnly());
		setRequired(t.isRequired());
		setResource(resource);
		setPropertyStore(t.getPropertyStore());
		setDefaultsToProperty(t.getDefaultsToProperty());
		setEncrypted(t.isEncrypted());
		setName(t.getName());
		setDescription(t.getDescription());
		setMetaData(t.getMetaData());
		getAttributes().putAll(t.getAttributes());
	}

	public void setPropertyStore(ResourcePropertyStore propertyStore) {
		this.propertyStore = propertyStore;
	}

	public String getValue() {
		return ((ResourcePropertyStore) propertyStore).getPropertyValue(this, resource);
	}

	public SimpleResource getResource() {
		return resource;
	}

	public void setResource(SimpleResource resource) {
		this.resource = resource;
	}

}
