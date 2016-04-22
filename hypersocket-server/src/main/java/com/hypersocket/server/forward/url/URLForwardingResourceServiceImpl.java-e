package com.hypersocket.server.forward.url;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.Property;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyFilter;
import com.hypersocket.properties.PropertyResolver;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.server.forward.ForwardingTransport;
import com.hypersocket.server.forward.url.events.URLForwardingResourceSessionClosed;
import com.hypersocket.server.forward.url.events.URLForwardingResourceSessionOpened;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Service
public class URLForwardingResourceServiceImpl extends AbstractAssignableResourceServiceImpl<URLForwardingResource>
		implements URLForwardingResourceService {

	public static final String RESOURCE_BUNDLE = "URLForwardingResourceService";
	
	@Autowired
	URLForwardingResourceRepository repository; 
	
	@Autowired
	EventService eventService; 
	
	@Autowired
	I18NService i18nService;
	
	protected URLForwardingResourceServiceImpl() {
		super("URLFowardingResource");
	}
	
	@PostConstruct
	private void postConstruct() {
		
		eventService.registerEvent(URLForwardingResourceSessionOpened.class, RESOURCE_BUNDLE);
		eventService.registerEvent(URLForwardingResourceSessionClosed.class, RESOURCE_BUNDLE);
		
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	@Override
	protected AbstractAssignableResourceRepository<URLForwardingResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return URLForwardingPermission.class;
	}

	@Override
	protected Class<URLForwardingResource> getResourceClass() {
		return URLForwardingResource.class;
	}

	@Override
	protected void fireResourceCreationEvent(URLForwardingResource resource) {
	}

	@Override
	protected void fireResourceCreationEvent(URLForwardingResource resource, Throwable t) {
	}

	@Override
	protected void fireResourceUpdateEvent(URLForwardingResource resource) {
	}

	@Override
	protected void fireResourceUpdateEvent(URLForwardingResource resource, Throwable t) {
	}

	@Override
	protected void fireResourceDeletionEvent(URLForwardingResource resource) {
	}

	@Override
	protected void fireResourceDeletionEvent(URLForwardingResource resource, Throwable t) {
	}

	@Override
	public void verifyResourceSession(URLForwardingResource resource, String hostname, int port,
			ForwardingTransport transport, Session session) throws AccessDeniedException {

		permissionService.assertResourceAccess(resource, session.getCurrentPrincipal());
	}
}
