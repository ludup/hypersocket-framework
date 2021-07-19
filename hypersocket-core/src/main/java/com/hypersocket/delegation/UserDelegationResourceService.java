package com.hypersocket.delegation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.ResourceException;


public interface UserDelegationResourceService extends
		AbstractAssignableResourceService<UserDelegationResource> {

	/**
	 * TODO rename this class to match your entity. Modify updateResource, createResource methods
	 * to take parameters for each additional field you have defined in your entity. 
	 */
	
	UserDelegationResource updateResource(UserDelegationResource resourceById, String name,
			Set<Role> roles, Map<String,String> properties) throws ResourceException, AccessDeniedException;

	UserDelegationResource createResource(String name, Set<Role> roles, Realm realm, Map<String,String> properties)
			throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(
			UserDelegationResource resource) throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException;

	void assertDelegation(Principal principal) throws AccessDeniedException;

	void assertDelegation(Collection<Principal> principals) throws AccessDeniedException;


}
