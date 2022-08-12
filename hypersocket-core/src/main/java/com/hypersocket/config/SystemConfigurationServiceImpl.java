/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;

@Service
public class SystemConfigurationServiceImpl extends
		AbstractAuthenticatedServiceImpl implements SystemConfigurationService {

	static Logger log = LoggerFactory
			.getLogger(SystemConfigurationServiceImpl.class);

	@Autowired
	private SystemConfigurationRepository repository;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private RealmService realmService; 

	@PostConstruct
	private void postConstruct() {

		eventService.registerEvent(ConfigurationChangedEvent.class,
				ConfigurationServiceImpl.RESOURCE_BUNDLE);
		eventService.registerEvent(ConfigurationValueChangedEvent.class,
				ConfigurationServiceImpl.RESOURCE_BUNDLE);

		repository.loadPropertyTemplates(SYSTEM_TEMPLATES_XML, getClass().getClassLoader());
	}

	@Override
	public String getValue(String resourceKey) {
		return repository.getValue(resourceKey);
	}

	@Override
	public Integer getIntValue(String name) throws NumberFormatException {
		return repository.getIntValue(name);
	}

	@Override
	public Boolean getBooleanValue(String name) {
		return repository.getBooleanValue(name);
	}

	@Override
	public void setValue(String resourceKey, String value)
			throws AccessDeniedException, ResourceException {
		try {
			assertPermission(ConfigurationPermission.UPDATE);
			String oldValue = repository.getValue(resourceKey);
			repository.setValue(resourceKey, value);
			fireChangeEvent(resourceKey, oldValue, value);
			eventService.publishEvent(new ConfigurationChangedEvent(this, true, getCurrentSession(), getCurrentRealm()));
		} catch (AccessDeniedException e) {
			log.error("Failed to set configuration.", e);
			fireChangeEvent(resourceKey, e);
			throw e;
		} catch (Throwable t) {
			log.error("Failed to set configuration.", t);
			fireChangeEvent(resourceKey, t);
			throw new ResourceChangeException(
					ConfigurationService.RESOURCE_BUNDLE,
					"error.unexpectedError", t.getMessage());
		}
	}

	@Override
	public void setValue(String resourceKey, Integer value)
			throws AccessDeniedException, ResourceException {
		setValue(resourceKey, String.valueOf(value));
	}

	@Override
	public void setValue(String name, Boolean value)
			throws AccessDeniedException, ResourceException {
		setValue(name, value.toString());
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories()
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories();
	}

	@Override
	public String[] getValues(String name) {
		return repository.getValues(name);
	}

	@Override
	public void setValues(Map<String, String> values)
			throws AccessDeniedException, ResourceException {

		try {
			assertPermission(ConfigurationPermission.UPDATE);

			Map<String, String> oldValues = new HashMap<String, String>();
			for (String resourceKey : values.keySet()) {
				oldValues.put(resourceKey, getValue(resourceKey));
			}
			repository.setValues(values);

			for (String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, oldValues.get(resourceKey),
						values.get(resourceKey));
			}
			
			eventService.publishEvent(new ConfigurationChangedEvent(this, true, getCurrentSession(), getCurrentRealm()));
		} catch (AccessDeniedException e) {
			for (String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, e);
			}
			throw e;
		} catch (Throwable t) {
			for (String resourceKey : values.keySet()) {
				fireChangeEvent(resourceKey, t);
			}
			throw new ResourceChangeException(
					ConfigurationService.RESOURCE_BUNDLE,
					"error.unexpectedError", t.getMessage());
		}
	}

	private void fireChangeEvent(String resourceKey, String oldValue,
			String newValue) {
		eventService.publishEvent(new ConfigurationValueChangedEvent(this, true,
				getCurrentSession(), repository
						.getPropertyTemplate(resourceKey), oldValue, newValue,
				repository.getPropertyTemplate(resourceKey).isHidden(), realmService.getSystemRealm()));
	}

	private void fireChangeEvent(String resourceKey, Throwable t) {
		eventService.publishEvent(new ConfigurationValueChangedEvent(this,
				resourceKey, t, getCurrentSession(), realmService.getSystemRealm()));
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories(group);
	}

	@Override
	public void setValues(String resourceKey, String[] array)
			throws ResourceException, AccessDeniedException {
		setValue(resourceKey, ResourceUtils.implodeValues(array));
	}
}
