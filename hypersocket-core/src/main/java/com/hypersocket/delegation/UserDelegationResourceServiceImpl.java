package com.hypersocket.delegation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.delegation.events.UserDelegationResourceCreatedEvent;
import com.hypersocket.delegation.events.UserDelegationResourceDeletedEvent;
import com.hypersocket.delegation.events.UserDelegationResourceEvent;
import com.hypersocket.delegation.events.UserDelegationResourceUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.resource.ResourceException;

@Service
public class UserDelegationResourceServiceImpl extends
		AbstractAssignableResourceServiceImpl<UserDelegationResource>implements UserDelegationResourceService {

	public static final String RESOURCE_BUNDLE = "UserDelegationResourceService";

	@Autowired
	private UserDelegationResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private RealmService realmService; 
	
	public UserDelegationResourceServiceImpl() {
		super("userDelegation");
	}

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.userDelegation");

		for (UserDelegationResourcePermission p : UserDelegationResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("userDelegationResourceTemplate.xml");

		eventService.registerEvent(UserDelegationResourceEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserDelegationResourceCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserDelegationResourceUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserDelegationResourceDeletedEvent.class, RESOURCE_BUNDLE);

		EntityResourcePropertyStore.registerResourceService(UserDelegationResource.class, repository);
		
		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException {
				
				UserDelegationResource resource = new UserDelegationResource();
				resource.setName("Default Delegation");
				resource.setRoleDelegates(new HashSet<>(Arrays.asList(permissionService.getRole(PermissionService.ROLE_EVERYONE, realm))));
				resource.setRoles(new HashSet<>(Arrays.asList(permissionService.getRole(PermissionService.ROLE_EVERYONE, realm))));
				resource.setRealm(realm);
				resource.setSystem(true);
				
				createResource(resource, null);
			}

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return repository.getCount(UserDelegationResource.class, new RealmCriteria(realm)) > 0;
			}
			
		});

	}

	@Override
	protected AbstractAssignableResourceRepository<UserDelegationResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<?> getPermissionType() {
		return UserDelegationResourcePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(UserDelegationResource resource) {
		eventService.publishEvent(new UserDelegationResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(UserDelegationResource resource, Throwable t) {
		eventService.publishEvent(new UserDelegationResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(UserDelegationResource resource) {
		eventService.publishEvent(new UserDelegationResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(UserDelegationResource resource, Throwable t) {
		eventService.publishEvent(new UserDelegationResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(UserDelegationResource resource) {
		eventService.publishEvent(new UserDelegationResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(UserDelegationResource resource, Throwable t) {
		eventService.publishEvent(new UserDelegationResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public UserDelegationResource updateResource(UserDelegationResource resource, String name, Set<Role> roles,
			Map<String, String> properties) throws ResourceException, AccessDeniedException {

		resource.setName(name);

		/**
		 * Set any additional fields on your resource here before calling
		 * updateResource.
		 * 
		 * Remember to fill in the fire*Event methods to ensure events are fired
		 * for all operations.
		 */
		updateResource(resource, roles, properties);

		return resource;
	}

	@Override
	public UserDelegationResource createResource(String name, Set<Role> roles, Realm realm,
			Map<String, String> properties) throws ResourceException, AccessDeniedException {

		UserDelegationResource resource = new UserDelegationResource();
		resource.setName(name);
		resource.setRealm(realm);
		resource.setRoles(roles);

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
	public Collection<PropertyCategory> getPropertyTemplate(UserDelegationResource resource)
			throws AccessDeniedException {

		assertPermission(UserDelegationResourcePermission.READ);
		return repository.getPropertyCategories(resource);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException {
		assertPermission(UserDelegationResourcePermission.READ);
		return repository.getPropertyCategories(null);
	}

	@Override
	protected Class<UserDelegationResource> getResourceClass() {
		return UserDelegationResource.class;
	}
	
	
	@Override
	public void assertDelegation(Principal principal) throws AccessDeniedException {
		
		if(getCurrentPrincipal().equals(principal)) {
			return;
		}
		
		if(permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			return;
		}
		
		Set<Role> roles = new HashSet<>();
		Set<Principal> users = new HashSet<>();
		Set<Principal> groups = new HashSet<>();
		
		Collection<UserDelegationResource> delegations = getPersonalResources();
		if(delegations.isEmpty()) {
			return;
		}
		for(UserDelegationResource delegation : delegations) {
			roles.addAll(delegation.getRoleDelegates());
			users.addAll(delegation.getUserDelegates());
			groups.addAll(delegation.getGroupDelegates());
		}
		
		if(users.contains(principal)) {
			return;
		}
		
		if(permissionService.hasRole(principal, roles)) {
			return;
		}
		
		Collection<Principal> principalGroups = realmService.getAssociatedPrincipals(principal, PrincipalType.GROUP);
		if(!Collections.disjoint(groups, principalGroups)) {
			return;
		}
		
		throw new AccessDeniedException("User does not have the delegate permission to act on this principal");
	}
	
	@Override
	public void assertDelegation(Collection<Principal> principals) throws AccessDeniedException {
		
		for(Principal principal : principals) {
			assertDelegation(principal);
		}
		
	}

}
