package com.hypersocket.resource;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;

@Service
public abstract class AbstractAssignableResourceServiceImpl<T extends AssignableResource>
		extends PasswordEnabledAuthenticatedServiceImpl implements
		AbstractAssignableResourceService<T>, EventPropertyCollector {

	static Logger log = LoggerFactory
			.getLogger(AbstractAssignableResourceRepositoryImpl.class);

	static final String RESOURCE_BUNDLE = "AssignableResourceService";

	@Autowired
	RealmService realmService;

	
	protected abstract AbstractAssignableResourceRepository<T> getRepository();

	protected abstract String getResourceBundle();

	public abstract Class<?> getPermissionType();

	protected PermissionType getUpdatePermission() {
		return getPermission("UPDATE");
	}

	protected PermissionType getCreatePermission() {
		return getPermission("CREATE");
	}

	protected PermissionType getDeletePermission() {
		return getPermission("DELETE");
	}

	protected PermissionType getReadPermission() {
		return getPermission("READ");
	}

	protected PermissionType getPermission(String name) {
		try {
			Field f = getPermissionType().getField(name);

			return (PermissionType) f.get(null);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not resolve update permission on PermissionType "
							+ getPermissionType().getName());
		}
	}
	
	@Override
	public Set<String> getPropertyNames(String resourceKey, Realm realm) {
		return getRepository().getPropertyNames();
	}

	@Override
	@SafeVarargs
	public final void createResource(T resource, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceCreationException,
			AccessDeniedException {

		assertPermission(getCreatePermission());

		try {
			getResourceByName(resource.getName());
			ResourceCreationException ex = new ResourceCreationException(RESOURCE_BUNDLE,
					"generic.alreadyExists.error", resource.getName());
			fireResourceCreationEvent(resource, ex);
			throw ex;
		} catch (ResourceNotFoundException ex) {
			try {
				if(resource.getRealm()==null) {
					resource.setRealm(getCurrentRealm());
				}
				beforeCreateResource(resource, properties);
				
				getRepository().saveResource(resource, properties, ops);
				
				afterCreateResource(resource, properties);

				fireResourceCreationEvent(resource);
			} catch (Throwable t) {
				log.error("Failed to create resource", t);
				fireResourceCreationEvent(resource, t);
				if (t instanceof ResourceCreationException) {
					throw (ResourceCreationException) t;
				} else {
					throw new ResourceCreationException(RESOURCE_BUNDLE,
							"generic.create.error", t.getMessage());
				}
			}
		}

	}

	protected void beforeCreateResource(T resource, Map<String,String> properties) throws ResourceCreationException {
		
	}

	protected void afterCreateResource(T resource, Map<String,String> properties) throws ResourceCreationException {
		
	}
	
	protected void beforeUpdateResource(T resource, Map<String,String> properties) throws ResourceChangeException {
		
	}
	
	protected void afterUpdateResource(T resource, Map<String,String> properties) throws ResourceChangeException {
		
	}
	
	protected abstract void fireResourceCreationEvent(T resource);

	protected abstract void fireResourceCreationEvent(T resource, Throwable t);

	@SafeVarargs
	public final void updateResource(T resource, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {
		assertPermission(getUpdatePermission());

		try {
			if(resource.getRealm()==null) {
				resource.setRealm(getCurrentRealm());
			}
			beforeUpdateResource(resource, properties);

			getRepository().saveResource(resource, properties, ops);

			afterUpdateResource(resource, properties);

			fireResourceUpdateEvent(resource);
		} catch (Throwable t) {
			fireResourceUpdateEvent(resource, t);
			if (t instanceof ResourceChangeException) {
				throw (ResourceChangeException) t;
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"generic.update.error", t.getMessage());
			}
		}
	}

	
	protected abstract void fireResourceUpdateEvent(T resource);

	protected abstract void fireResourceUpdateEvent(T resource, Throwable t);

	@Override
	public void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException {

		assertPermission(getDeletePermission());

		try {
			getRepository().deleteResource(resource);
			fireResourceDeletionEvent(resource);
		} catch (Throwable t) {
			fireResourceDeletionEvent(resource, t);
			if (t instanceof ResourceChangeException) {
				throw (ResourceChangeException) t;
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"generic.delete.error", t.getMessage());
			}
		}

	}

	protected abstract void fireResourceDeletionEvent(T resource);

	protected abstract void fireResourceDeletionEvent(T resource, Throwable t);

	@Override
	public List<T> getResources(Realm realm) throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().getResources(realm);

	}

	@Override
	public List<T> getResources() {
		return getRepository().getResources(getCurrentRealm());
	}
	
	public List<T> allResources() {
		return getRepository().allResources();
	}
	
	@Override
	public Collection<T> searchPersonalResources(Principal principal, String search, int start,
			int length, ColumnSort[] sorting) {

		return getRepository().searchAssignedResources(principal, search, start, length, sorting);
	}
	
	@Override
	public Collection<T> getPersonalResources(Principal principal) throws AccessDeniedException {
		return getRepository().getAssignedResources(realmService.getAssociatedPrincipals(principal));
	}
	
	@Override
	public long getPersonalResourceCount(Principal principal, String search) {

		return getRepository().getAssignedResourceCount(principal, search);
	}

	@Override
	public List<T> searchResources(Realm realm, String search, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().search(realm, search, start, length, sorting);
	}

	@Override
	public long getResourceCount(Realm realm, String search)
			throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().getResourceCount(realm, search);
	}

	@Override
	public T getResourceByName(String name) throws ResourceNotFoundException {
		T resource = getRepository().getResourceByName(name, getCurrentRealm());
		if (resource == null) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		return resource;
	}

	@Override
	public T getResourceById(Long id) throws ResourceNotFoundException {
		T resource = getRepository().getResourceById(id);
		if (resource == null) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceId", id);
		}
		return resource;
	}

	@Override
	public Collection<T> getResources(Principal principal)
			throws AccessDeniedException {

		if (log.isDebugEnabled()) {
			log.debug("Looking up resources for principal "
					+ principal.getRealm() + "/" + principal.getPrincipalName());
		}

		return getRepository().getAssignedResources(
				realmService.getAssociatedPrincipals(principal));
	}

	protected void assertResourceAssignment(Principal principal,
			Resource resource) throws AccessDeniedException {

		for (Resource r : getResources(principal)) {
			if (r.equals(resource)) {
				return;
			}
		}
		throw new AccessDeniedException("Principal " + principal.getName()
				+ " does not have access to " + resource.getName());
	}
}
