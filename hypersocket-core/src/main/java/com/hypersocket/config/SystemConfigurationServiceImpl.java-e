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

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.resource.ResourceChangeException;

@Service
public class SystemConfigurationServiceImpl extends
		AbstractAuthenticatedServiceImpl implements SystemConfigurationService {

	static Logger log = LoggerFactory
			.getLogger(SystemConfigurationServiceImpl.class);

	@Autowired
	SystemConfigurationRepository repository;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {

		eventService.registerEvent(ConfigurationChangedEvent.class,
				ConfigurationServiceImpl.RESOURCE_BUNDLE);

		repository.loadPropertyTemplates("systemTemplates.xml");
	}

	protected void onValueChanged(PropertyTemplate template, String oldValue,
			String value) {
		eventService.publishEvent(new ConfigurationChangedEvent(this, true,
				getCurrentSession(), template, oldValue, value, template
						.isHidden()));
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
			throws AccessDeniedException, ResourceChangeException {
		try {
			assertPermission(ConfigurationPermission.UPDATE);
			String oldValue = repository.getValue(resourceKey);
			repository.setValue(resourceKey, value);
			fireChangeEvent(resourceKey, oldValue, value);
		} catch (AccessDeniedException e) {
			fireChangeEvent(resourceKey, e);
			throw e;
		} catch (Throwable t) {
			fireChangeEvent(resourceKey, t);
			throw new ResourceChangeException(
					ConfigurationService.RESOURCE_BUNDLE,
					"error.unexpectedError", t.getMessage());
		}
	}

	@Override
	public void setValue(String resourceKey, Integer value)
			throws AccessDeniedException {
		setValue(resourceKey, value);
	}

	@Override
	public void setValue(String name, Boolean value)
			throws AccessDeniedException, ResourceChangeException {
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
			throws AccessDeniedException, ResourceChangeException {

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
		eventService.publishEvent(new ConfigurationChangedEvent(this, true,
				getCurrentSession(), repository
						.getPropertyTemplate(resourceKey), oldValue, newValue,
				repository.getPropertyTemplate(resourceKey).isHidden()));
	}

	private void fireChangeEvent(String resourceKey, Throwable t) {
		eventService.publishEvent(new ConfigurationChangedEvent(this,
				resourceKey, t, getCurrentSession()));
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group)
			throws AccessDeniedException {
		assertPermission(ConfigurationPermission.READ);
		return repository.getPropertyCategories(group);
	}

	@Override
	public void setValues(String resourceKey, String[] array)
			throws ResourceChangeException, AccessDeniedException {
		setValue(resourceKey, ResourceUtils.implodeValues(array));
	}
}
