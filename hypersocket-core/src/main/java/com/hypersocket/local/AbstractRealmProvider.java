/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.attributes.user.UserAttributeService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.resource.Resource;


public abstract class AbstractRealmProvider extends ResourceTemplateRepositoryImpl implements RealmProvider {

	@Autowired
	protected UserAttributeService userAttributeService;
	
	public boolean supportsTemplates() {
		return false;
	}
	
	@Override
	public Set<String> getCustomPropertyNames(Realm realm) {
		return Collections.emptySet();
	}

	@JsonIgnore
	public ResourceTemplateRepository getTemplateRepository() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Collection<PropertyCategory> getPrincipalTemplate(Resource resource) {
		if(supportsTemplates()) {
			return getTemplateRepository().getPropertyCategories(resource);
		} else {
			return Collections.<PropertyCategory>emptyList();
		}
	}

	@Override
	public Collection<PropertyCategory> getPrincipalTemplate() {
		if(supportsTemplates()) {
			return getTemplateRepository().getPropertyCategories(null);
		} else {
			return Collections.<PropertyCategory>emptyList();
		}
	}
	
	@Override
	public Map<String,String> getPrincipalTemplateProperties(Resource resource) {
		if(supportsTemplates()) {
			return getTemplateRepository().getProperties(resource);
		} else {
			return new HashMap<String,String>();
		}
	}
}
