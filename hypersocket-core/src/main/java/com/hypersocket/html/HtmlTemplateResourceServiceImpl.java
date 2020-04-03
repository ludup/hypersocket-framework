package com.hypersocket.html;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.html.events.HtmlTemplateResourceCreatedEvent;
import com.hypersocket.html.events.HtmlTemplateResourceDeletedEvent;
import com.hypersocket.html.events.HtmlTemplateResourceEvent;
import com.hypersocket.html.events.HtmlTemplateResourceUpdatedEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceException;

@Service
public class HtmlTemplateResourceServiceImpl extends
		AbstractResourceServiceImpl<HtmlTemplateResource> implements
		HtmlTemplateResourceService {

	public static final String RESOURCE_BUNDLE = "HtmlTemplateResourceService";

	@Autowired
	private HtmlTemplateResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	public HtmlTemplateResourceServiceImpl() {
		super("HtmlTemplate");
	}
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.htmlTemplates");

		for (HtmlTemplateResourcePermission p : HtmlTemplateResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("htmlTemplateResourceTemplate.xml");

		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(
				HtmlTemplateResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HtmlTemplateResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HtmlTemplateResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HtmlTemplateResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);

		EntityResourcePropertyStore.registerResourceService(HtmlTemplateResource.class, repository);
	}

	@Override
	protected AbstractResourceRepository<HtmlTemplateResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<HtmlTemplateResourcePermission> getPermissionType() {
		return HtmlTemplateResourcePermission.class;
	}
	
	protected Class<HtmlTemplateResource> getResourceClass() {
		return HtmlTemplateResource.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(HtmlTemplateResource resource) {
		eventService.publishEvent(new HtmlTemplateResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(HtmlTemplateResource resource,
			Throwable t) {
		eventService.publishEvent(new HtmlTemplateResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(HtmlTemplateResource resource) {
		eventService.publishEvent(new HtmlTemplateResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(HtmlTemplateResource resource,
			Throwable t) {
		eventService.publishEvent(new HtmlTemplateResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(HtmlTemplateResource resource) {
		eventService.publishEvent(new HtmlTemplateResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(HtmlTemplateResource resource,
			Throwable t) {
		eventService.publishEvent(new HtmlTemplateResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public HtmlTemplateResource updateResource(HtmlTemplateResource resource,
			String name, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

		resource.setName(name);

		/**
		 * Set any additional fields on your resource here before calling
		 * updateResource.
		 * 
		 * Remember to fill in the fire*Event methods to ensure events are fired
		 * for all operations.
		 */
		updateResource(resource, properties);

		return resource;
	}

	@Override
	public HtmlTemplateResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceException,
			AccessDeniedException {

		HtmlTemplateResource resource = new HtmlTemplateResource();
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
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {

		assertPermission(HtmlTemplateResourcePermission.READ);

		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			HtmlTemplateResource resource) throws AccessDeniedException {

		assertPermission(HtmlTemplateResourcePermission.READ);

		return repository.getPropertyCategories(resource);
	}

}
