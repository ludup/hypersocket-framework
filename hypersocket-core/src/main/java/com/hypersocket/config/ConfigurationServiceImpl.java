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
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;

@Service
public class ConfigurationServiceImpl extends AbstractAuthenticatedServiceImpl
		implements ConfigurationService {

	static Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	@Autowired
	ConfigurationRepository repository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventPublisher;

	@Autowired
	I18NService i18nService; 
	
	Locale defaultLocale = null;
	
	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.configuration");

		for (ConfigurationPermission p : ConfigurationPermission.values()) {
			permissionService.registerPermission(p,cat);
		}

		repository.loadPropertyTemplates("propertyTemplates.xml");
		
	}

	@Override
	public String getValue(Realm realm, String resourceKey) {
		return repository.getValue(realm, resourceKey);
	}
	
	@Override
	public String getValue(String resourceKey) {
		return repository.getValue(getCurrentRealm(), resourceKey);
	}

	@Override
	public Integer getIntValue(Realm realm, String name) throws NumberFormatException {
		return repository.getIntValue(realm, name);
	}
	
	@Override
	public Integer getIntValue(String name) throws NumberFormatException {
		return repository.getIntValue(getCurrentRealm(), name);
	}
	
	@Override
	public Boolean getBooleanValue(Realm realm, String name) {
		return repository.getBooleanValue(realm, name);
	}
	
	@Override 
	public Double getDoubleValue(Realm realm, String name) {
		return repository.getDoubleValue(realm, name);
	}
	
	@Override
	public void setDoubleValue(Realm realm, String name, Double value) {
		repository.setDoubleValue(realm, name, value);
	}
	
	@Override
	public Boolean getBooleanValue(String name) {
		return repository.getBooleanValue(getCurrentRealm(), name);
	}

	@Override
	public void setValue(String resourceKey, String value)
			throws AccessDeniedException, ResourceChangeException {
		setValue(getCurrentRealm(), resourceKey, value);
	}
	
	@Override
	public void setValue(Realm realm, String resourceKey, String value)
			throws AccessDeniedException, ResourceChangeException {
		try {
			assertPermission(ConfigurationPermission.UPDATE);
			String oldValue = repository.getValue(realm, resourceKey);
			repository.setValue(realm, resourceKey, value);
			fireChangeEvent(resourceKey, oldValue, value, repository.getPropertyTemplate(realm, resourceKey).isHidden());
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
		return repository.getPropertyCategories(getCurrentRealm());
	}

	@Override
	public String[] getValues(String name) {
		return repository.getValues(getCurrentRealm(), name);
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
			repository.setValues(getCurrentRealm(), values);
			
			for(String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, oldValues.get(resourceKey), values.get(resourceKey), repository.getPropertyTemplate(getCurrentRealm(), resourceKey).isHidden());
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
	
	private void fireChangeEvent(String resourceKey, String oldValue, String newValue, boolean hidden) {
		if(resourceKey.equals("current.locale")) {
			defaultLocale = i18nService.getLocale(newValue);
		}
		eventPublisher.publishEvent(new ConfigurationChangedEvent(this, true,
				getCurrentSession(), repository.getPropertyTemplate(getCurrentRealm(), resourceKey), oldValue, newValue, hidden));
		
	}

	private void fireChangeEvent(String resourceKey, Throwable t) {
		eventPublisher.publishEvent(new ConfigurationChangedEvent(this, resourceKey, t,
				getCurrentSession()));
	}
	
	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		Collection<PropertyCategory> cats = repository.getPropertyCategories(getCurrentRealm(), group);
		return cats;
	}

	@Override
	public String[] getValues(Realm realm, String name) {
		return repository.getValues(realm, name);
	}
	
	@Override
	public void setValues(String resourceKey, String[] array) throws ResourceChangeException, AccessDeniedException {
		setValue(resourceKey, ResourceUtils.implodeValues(array));
	}
	
	@Override
	public boolean hasUserLocales() {
		return getBooleanValue("user.locales");
	}
	
	@Override
	public synchronized Locale getDefaultLocale() {
		if(defaultLocale==null) {
			String locale = getValue("current.locale");
			return defaultLocale = i18nService.getLocale(locale);
		} else {
			return defaultLocale;
		}
	}

	@Override
	public String getDecryptedValue(Realm realm, String resourceKey) {
		return repository.getDecryptedValue(realm, resourceKey);
	}
	
}
