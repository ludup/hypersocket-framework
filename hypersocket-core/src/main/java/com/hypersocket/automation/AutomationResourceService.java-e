package com.hypersocket.automation;

import java.util.Collection;
import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface AutomationResourceService extends
		AbstractResourceService<AutomationResource> {

	AutomationResource updateResource(AutomationResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	AutomationResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(AutomationResource resource)
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(String resourceKey)
			throws AccessDeniedException;

	Collection<String> getTasks() throws AccessDeniedException;


}
