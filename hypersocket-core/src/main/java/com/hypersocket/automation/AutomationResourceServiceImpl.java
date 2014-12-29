package com.hypersocket.automation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.automation.events.AutomationResourceCreatedEvent;
import com.hypersocket.automation.events.AutomationResourceDeletedEvent;
import com.hypersocket.automation.events.AutomationResourceUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

@Service
public class AutomationResourceServiceImpl extends
		AbstractResourceServiceImpl<AutomationResource> implements
		AutomationResourceService {

	public static final String RESOURCE_BUNDLE = "AutomationResourceService";

	private Map<String,AutomationProvider> providers = new HashMap<String,AutomationProvider>();
	
	@Autowired
	AutomationResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@Autowired
	EntityResourcePropertyStore entityPropertyStore; 
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.automation");

		for (AutomationResourcePermission p : AutomationResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("automationTemplate.xml");


		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(
				AutomationResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				AutomationResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				AutomationResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);

		entityPropertyStore.registerResourceService(AutomationResource.class, this);
	}

	@Override
	public void registerProvider(AutomationProvider provider) {
		for(String resourceKey : provider.getResourceKeys()) {
			providers.put(resourceKey, provider);
		}
	}
	
	@Override
	public void unregisterProvider(AutomationProvider provider) {
		for(String resourceKey : provider.getResourceKeys()) {
			providers.remove(resourceKey);
		}
	}
	
	@Override
	protected AbstractResourceRepository<AutomationResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<AutomationResourcePermission> getPermissionType() {
		return AutomationResourcePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource) {
		eventService.publishEvent(new AutomationResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(AutomationResource resource,
			Throwable t) {
		eventService.publishEvent(new AutomationResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public AutomationResource updateResource(AutomationResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);

		updateResource(resource, properties);

		return resource;
	}

	protected void afterCreateResource(AutomationResource resource, Map<String,String> properties) throws ResourceCreationException {
		AutomationProvider provider = getAutomationProvider(resource);
		provider.getRepository().setValues(resource, properties);
	}
	
	protected void afterUpdateResource(AutomationResource resource, Map<String,String> properties) throws ResourceChangeException {
		AutomationProvider provider = getAutomationProvider(resource);
		provider.getRepository().setValues(resource, properties);
	}

	@Override
	public AutomationResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		AutomationResource resource = new AutomationResource();
		resource.setName(name);
		resource.setRealm(realm);
		/**
		 * Set any additional fields on your resource here before calling
		 * createResource.
		 * 
		 * Remember to fill in the fire*Event methods to ensure events are fired
		 * for all operations.
		 */
		createResource(resource, properties);

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(String resourceKey)
			throws AccessDeniedException {

		assertPermission(AutomationResourcePermission.READ);

		Collection<PropertyCategory> results = repository.getPropertyCategories(null);
		
		AutomationProvider provider = getAutomationProvider(resourceKey);
		
		results.addAll(provider.getRepository().getPropertyCategories(null));
		
		return results;
	}

	
	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			AutomationResource resource) throws AccessDeniedException {
	
		assertPermission(AutomationResourcePermission.READ);

		Collection<PropertyCategory> results = repository.getPropertyCategories(resource);
		
		AutomationProvider provider = getAutomationProvider(resource);
		
		results.addAll(provider.getRepository().getPropertyCategories(resource));
		
		return results;
	}

	private AutomationProvider getAutomationProvider(AutomationResource resource) {
		return getAutomationProvider(resource.getResourceKey());
	}

	private AutomationProvider getAutomationProvider(String resourceKey) {
		if(!providers.containsKey(resourceKey)) {
			throw new IllegalStateException("AutomationProvider not available for resource key " + resourceKey);
		}
		return providers.get(resourceKey);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {
		throw new IllegalStateException("AutomationResource needs provider resource key to return property templates");
	}

	@Override
	public Collection<String> getTasks() throws AccessDeniedException {
		
		assertPermission(AutomationResourcePermission.READ);
		
		return providers.keySet();
	}
}
