package com.hypersocket.password.policy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;


public interface PasswordPolicyResourceService extends
		AbstractAssignableResourceService<PasswordPolicyResource> {

	PasswordPolicyResource updateResource(PasswordPolicyResource resourceById, String name,
			Set<Role> roles, Map<String,String> properties) throws ResourceException, AccessDeniedException;

	PasswordPolicyResource createResource(String name, Set<Role> roles, Realm realm, Map<String,String> properties)
			throws AccessDeniedException, ResourceException;

	Collection<PropertyCategory> getPropertyTemplate(
			PasswordPolicyResource resource) throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException;

	PasswordPolicyResource resolvePolicy(Principal currentPrincipal) throws ResourceNotFoundException;

	void registerPolicyResolver(String resourceKey, PolicyResolver resolver);

	PasswordPolicyResource getPolicyByDN(String dn, Realm realm);
	
	Iterator<PasswordPolicyResource> iterate(Realm realm);

	PasswordPolicyResource getDefaultPasswordPolicy(Realm realm);
	
	PasswordPolicyResource getDefaultPolicy(Realm realm, String moduleName);

	Collection<PasswordPolicyResource> getPoliciesByGroup(Principal principal);

	String generatePassword(PasswordPolicyResource policy);
	
	String generatePassword(PasswordPolicyResource policy, int length);

	void deleteRealm(Realm realm);

	PasswordPolicyResource getLocalPolicy(Realm realm);


}
