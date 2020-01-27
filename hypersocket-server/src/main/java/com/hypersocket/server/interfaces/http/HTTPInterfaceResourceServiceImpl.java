package com.hypersocket.server.interfaces.http;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceCreatedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceDeletedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceUpdatedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceStartedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceStoppedEvent;

@Service
public class HTTPInterfaceResourceServiceImpl extends
		AbstractResourceServiceImpl<HTTPInterfaceResource> implements
		HTTPInterfaceResourceService {

	public static final String RESOURCE_BUNDLE = "HTTPInterfaceResourceService";

	@Autowired
	private HTTPInterfaceResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private EventService eventService;

	public HTTPInterfaceResourceServiceImpl() {
		super("HTTPInterface");
	}
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

//		PermissionCategory cat = permissionService.registerPermissionCategory(
//				RESOURCE_BUNDLE, "category.httpInterfaces");
//
//		for (HTTPInterfaceResourcePermission p : HTTPInterfaceResourcePermission.values()) {
//			permissionService.registerPermission(p, cat);
//		}

		repository.loadPropertyTemplates("httpInterfaceResourceTemplate.xml");

		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(
				HTTPInterfaceResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HTTPInterfaceResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HTTPInterfaceResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				HTTPInterfaceResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(HTTPInterfaceStartedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(HTTPInterfaceStoppedEvent.class, RESOURCE_BUNDLE);
		
		EntityResourcePropertyStore.registerResourceService(HTTPInterfaceResource.class, repository);
	}
	
	@Override
	protected boolean isSystemResource() {
		return true;
	}

	@Override
	protected AbstractResourceRepository<HTTPInterfaceResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return null;
	}
	
	protected Class<HTTPInterfaceResource> getResourceClass() {
		return HTTPInterfaceResource.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(HTTPInterfaceResource resource) {
		eventService.publishEvent(new HTTPInterfaceResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(HTTPInterfaceResource resource,
			Throwable t) {
		eventService.publishEvent(new HTTPInterfaceResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(HTTPInterfaceResource resource) {
		eventService.publishEvent(new HTTPInterfaceResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(HTTPInterfaceResource resource,
			Throwable t) {
		eventService.publishEvent(new HTTPInterfaceResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(HTTPInterfaceResource resource) {
		eventService.publishEvent(new HTTPInterfaceResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(HTTPInterfaceResource resource,
			Throwable t) {
		eventService.publishEvent(new HTTPInterfaceResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public HTTPInterfaceResource updateResource(HTTPInterfaceResource resource,
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
	protected void beforeDeleteResource(HTTPInterfaceResource resource) throws ResourceException, AccessDeniedException {
		if(repository.allRealmsResourcesCount()==1) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.oneInterfaceRequired");
		}
		super.beforeDeleteResource(resource);
	}

	@Override
	public HTTPInterfaceResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceException,
			AccessDeniedException {

		HTTPInterfaceResource resource = new HTTPInterfaceResource();
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

		assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);

		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			HTTPInterfaceResource resource) throws AccessDeniedException {

		assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);

		return repository.getPropertyCategories(resource);
	}

}
