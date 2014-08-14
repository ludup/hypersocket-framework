/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.config;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;

@Transactional
public class ConfigurationServiceImpl extends AuthenticatedServiceImpl
		implements ConfigurationService {

	static Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	@Autowired
	ConfigurationRepository repository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.configuration");

		for (ConfigurationPermission p : ConfigurationPermission.values()) {
			permissionService.registerPermission(p.getResourceKey(), p.isSystem(), cat);
		}

		repository.loadPropertyTemplates("propertyTemplates.xml");
	}

	protected void onValueChanged(PropertyTemplate template, String oldValue,
			String value) {
		eventPublisher.publishEvent(new ConfigurationChangedEvent(this, true,
				getCurrentSession(), template, oldValue, value));
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
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.UPDATE);
		repository.setValue(getPropertyRealm(), resourceKey, value);
	}

	@Override
	public void setValue(String resourceKey, Integer value)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.UPDATE);
		repository.setValue(getPropertyRealm(), resourceKey, value);
	}

	@Override
	public void setValue(String name, Boolean value)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.UPDATE);
		repository.setValue(getPropertyRealm(), name, value);
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

		assertPermission(ConfigurationPermission.UPDATE);
		repository.setValues(getPropertyRealm(), values);

	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories(getPropertyRealm(), group);
	}
}
