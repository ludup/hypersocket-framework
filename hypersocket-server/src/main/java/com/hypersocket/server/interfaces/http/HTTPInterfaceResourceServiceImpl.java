package com.hypersocket.server.interfaces.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionAdapter;
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

	static Logger LOG = LoggerFactory.getLogger(HTTPInterfaceResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "HTTPInterfaceResourceService";

	@Autowired
	private HTTPInterfaceResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private SystemConfigurationService configurationService;

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
		
		/* Synchronize the port for Default HTTP and Default HTTPS 
		 * interfaces from the system configuration properties so that
		 * the port can be overridden by changing hypersocket.properties
		 * (or re-running the installer).
		 */
		HTTPInterfaceResource http = repository.getResourceByName("Default HTTP", realmService.getSystemRealm());
		int defaultHttpsPort = configurationService.getIntValue("https.port");
		int defaultHttpPort = configurationService.getIntValue("http.port");
		if (http == null) {
			LOG.warn("Could not find default HTTP interface, cannot make it system");
		} else {
			http.setSystem(true);
			if(!Objects.equals(defaultHttpPort, http.getPort())) {
				LOG.info(String.format("Detected change to default HTTP port, changing from %d to %d", http.getPort(), defaultHttpPort));
				http.setPort(defaultHttpPort);
			}
			if(!Objects.equals(defaultHttpsPort, http.getRedirectPort())) {
				LOG.info(String.format("Detected change to default redirect HTTPS port, changing from %d to %d", http.getRedirectPort(), defaultHttpsPort));
				http.setRedirectPort(defaultHttpsPort);
			}
			try {
				repository.saveResource(http);
			} catch (ResourceException e) {
				throw new IllegalStateException(e);
			}
		}
		HTTPInterfaceResource https = repository.getResourceByName("Default HTTPS", realmService.getSystemRealm());
		if (https == null) {
			LOG.warn("Could not find default HTTPS interface, cannot make it system");
		} else {
			https.setSystem(true);
			if(!Objects.equals(defaultHttpsPort, https.getPort())) {
				LOG.info(String.format("Detected change to default HTTPS port, changing from %d to %d", https.getPort(), defaultHttpsPort));
				https.setPort(defaultHttpsPort);
			}
			try {
				repository.saveResource(https);
			} catch (ResourceException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	@EventListener
	@Override
	public void httpInterfaceUpdated(HTTPInterfaceResourceUpdatedEvent resourceUpdated) throws AccessDeniedException, ResourceException {
		int defaultHttpsPort = configurationService.getIntValue("https.port");
		int defaultHttpPort = configurationService.getIntValue("http.port");
		if(resourceUpdated.isSuccess()) {
			HTTPInterfaceResource http = (HTTPInterfaceResource)resourceUpdated.getResource();
			if(http.isSystem()) {
				if(http.getProtocol().equals(HTTPProtocol.HTTPS) && !Objects.equals(defaultHttpsPort, http.getPort())) {
					LOG.info(String.format("Detected change to default HTTPS port, changing from %d to %d", defaultHttpsPort, http.getPort()));
					configurationService.setValue("https.port", http.getPort());
				}
				else if(http.getProtocol().equals(HTTPProtocol.HTTP) && !Objects.equals(defaultHttpPort, http.getPort())) {
					LOG.info(String.format("Detected change to default HTTP port, changing from %d to %d", defaultHttpPort, http.getPort()));
					configurationService.setValue("http.port", http.getPort());
				}
			}
		}
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
		
		checkForPortAddressConflicts(resource, properties);
		
		updateResource(resource, properties);

		return resource;
	}

	protected void checkForPortAddressConflicts(HTTPInterfaceResource resource, Map<String, String> properties) throws ResourceChangeException {
		/* Check no conflicts */
		String newAddresses =  properties.containsKey("interfaces") ? properties.get("interfaces") : resource.getInterfaces();
		Integer newPort = properties.containsKey("port") ? Integer.parseInt(properties.get("port")) : resource.getPort();
				
		for(HTTPInterfaceResource res : allRealmsResources()) {
			if(!res.equals(resource)) {
				boolean addrMatch = Boolean.TRUE.equals(res.getAllInterfaces()) || Boolean.TRUE.equals(resource.getAllInterfaces()) ||
							anyMatch(ResourceUtils.explodeValues(res.getInterfaces()), ResourceUtils.explodeValues(newAddresses));
				boolean portMatch = Objects.equals(res.getPort(), newPort);
				if(addrMatch && portMatch)
					throw new ResourceChangeException(RESOURCE_BUNDLE, "error.conflictingBind", res.getName());
			}
		}
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
		createResource(resource, properties, new TransactionAdapter<HTTPInterfaceResource>() {

			@Override
			public void beforeOperation(HTTPInterfaceResource resource, Map<String, String> properties)
					throws ResourceException {
				if(resource.getProtocol()==HTTPProtocol.HTTPS) {
					if(Objects.isNull(resource.getCertificate())) {
						throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noCertificate");
					}
				}		
				checkForPortAddressConflicts(resource, properties);
			}
			
		});

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
	
	static boolean anyMatch(String[] list1, String[] list2) {
		List<String> l1= Arrays.asList(list1);
		List<String> l2= Arrays.asList(list2);
		for(String s1 : l1) {
			if(l2.contains(s1))
				return true;
		}
		for(String s2 : l2) {
			if(l1.contains(s2))
				return true;
		}
		return false;
	}

}
