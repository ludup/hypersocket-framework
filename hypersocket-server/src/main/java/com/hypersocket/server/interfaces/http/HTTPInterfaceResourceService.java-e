package com.hypersocket.server.interfaces.http;

import java.util.Collection;
import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface HTTPInterfaceResourceService extends
		AbstractResourceService<HTTPInterfaceResource> {

	HTTPInterfaceResource updateResource(HTTPInterfaceResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	HTTPInterfaceResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(HTTPInterfaceResource resource)
			throws AccessDeniedException;

}
