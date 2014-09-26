/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;

@Service
public class ConfigurationServiceImpl extends AuthenticatedServiceImpl
		implements ConfigurationService {

	static Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	@Autowired
	ConfigurationRepository repository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventPublisher;

	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.configuration");

		for (ConfigurationPermission p : ConfigurationPermission.values()) {
			permissionService.registerPermission(p,cat);
		}

		repository.loadPropertyTemplates("propertyTemplates.xml");
		
		eventPublisher.registerEvent(ConfigurationChangedEvent.class, RESOURCE_BUNDLE);
	}

	protected Realm getPropertyRealm() {
		/**
		 * Return the realm we should record against. We return null if the
		 * realm is the default system realm since we want that realm to hold
		 * all default settings.
		 */
		if (getCurrentRealm()==null || getCurrentRealm().isSystem()) {
			return null;
		} else {
			return getCurrentRealm();
		}
	}

	@Override
	public String getValue(Realm realm, String resourceKey) {
		return repository.getValue(realm, resourceKey);
	}
	
	@Override
	public String getValue(String resourceKey) {
		return repository.getValue(getPropertyRealm(), resourceKey);
	}

	@Override
	public Integer getIntValue(Realm realm, String name) throws NumberFormatException {
		return repository.getIntValue(realm, name);
	}
	
	@Override
	public Integer getIntValue(String name) throws NumberFormatException {
		return repository.getIntValue(getPropertyRealm(), name);
	}
	
	@Override
	public Boolean getBooleanValue(Realm realm, String name) {
		return repository.getBooleanValue(realm, name);
	}
	
	@Override
	public Boolean getBooleanValue(String name) {
		return repository.getBooleanValue(getPropertyRealm(), name);
	}

	@Override
	public void setValue(String resourceKey, String value)
			throws AccessDeniedException, ResourceChangeException {
		try {
			assertPermission(ConfigurationPermission.UPDATE);
			repository.setValue(getPropertyRealm(), resourceKey, value);
		} catch (AccessDeniedException e) {
			fireChangeEvent(resourceKey, e);
			throw e;
		} catch (Throwable t) {
			fireChangeEvent(resourceKey, t);
			throw new ResourceChangeException(ConfigurationService.RESOURCE_BUNDLE, "error.unexpectedError", t.getMessage());
		}
	}

	@Override
	public void setValue(String resourceKey, Integer value)
			throws AccessDeniedException, ResourceChangeException {
		setValue(resourceKey, String.valueOf(value));
	}

	@Override
	public void setValue(String name, Boolean value)
			throws AccessDeniedException, ResourceChangeException {
		setValue(name, String.valueOf(value));
	}
	

	@Override
	public Collection<PropertyCategory> getPropertyCategories()
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories(getPropertyRealm());
	}

	@Override
	public String[] getValues(String name) {
		return repository.getValues(getPropertyRealm(), name);
	}

	@Override
	public void setValues(Map<String, String> values)
			throws AccessDeniedException, ResourceChangeException {

		try {
			assertPermission(ConfigurationPermission.UPDATE);
			
			Map<String,String> oldValues = new HashMap<String,String>();
			for(String resourceKey : values.keySet()) {
				oldValues.put(resourceKey, getValue(resourceKey));
			}
			repository.setValues(getPropertyRealm(), values);
			
			for(String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, oldValues.get(resourceKey), values.get(resourceKey));
			}
		} catch (AccessDeniedException e) {
			for(String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, e);
			}
			throw e;
		} catch (Throwable t) {
			for(String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, t);
			}
			throw new ResourceChangeException(ConfigurationService.RESOURCE_BUNDLE, "error.unexpectedError", t.getMessage());
		}
	}
	
	private void fireChangeEvent(String resourceKey, String oldValue, String newValue) {
		eventPublisher.publishEvent(new ConfigurationChangedEvent(this, true,
				getCurrentSession(), repository.getPropertyTemplate(resourceKey), oldValue, newValue));
	}

	private void fireChangeEvent(String resourceKey, Throwable t) {
		eventPublisher.publishEvent(new ConfigurationChangedEvent(this, resourceKey, t,
				getCurrentSession()));
	}
	
	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories(getPropertyRealm(), group);
	}

	@Override
	public String[] getValues(Realm realm, String name) {
		return repository.getValues(realm, name);
	}
}
