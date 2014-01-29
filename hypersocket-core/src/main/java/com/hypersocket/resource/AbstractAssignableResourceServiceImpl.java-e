package com.hypersocket.resource;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

@Repository
@Transactional
public abstract class AbstractAssignableResourceServiceImpl<T extends AssignableResource>
		extends AuthenticatedServiceImpl implements AbstractAssignableResourceService<T> {

	static Logger log = LoggerFactory.getLogger(AbstractAssignableResourceRepositoryImpl.class);
	
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
			
			return (PermissionType)f.get(null);
		} catch (Exception e) {
			throw new IllegalStateException("Could not resolve update permission on PermissionType " + getPermissionType().getName());
		}
	}
	
	@Override
	public void createResource(T resource) throws ResourceChangeException, AccessDeniedException {
		assertPermission(getCreatePermission());
		
		getRepository().saveResource(resource);
	}
	public void updateResource(T resource) throws ResourceChangeException, AccessDeniedException {
		assertPermission(getUpdatePermission());
		
		getRepository().saveResource(resource);
	}
	
	@Override
	public void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException {

		assertPermission(getDeletePermission());

		getRepository().deleteResource(resource);

	}

	@Override
	public List<T> getResources(Realm realm) throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().getResources(realm);

	}
	
	@Override
	public List<T> getResources() {
		return getRepository().getResources();
	}
	
	@Override
	public T getResourceByName(String name) throws ResourceNotFoundException {
		T resource = getRepository().getResourceByName(name);
		if (resource == null) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		return resource;
	}

	@Override
	public T getResourceById(Long id)
			throws ResourceNotFoundException {
		T resource = getRepository().getResourceById(id);
		if (resource == null) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceId", id);
		}
		return resource;
	}
	
	@Override
	public List<T> getResources(Principal principal)
			throws AccessDeniedException {

		if (log.isDebugEnabled()) {
			log.debug("Looking up NetworkResources for principal "
					+ principal.getRealm() + "/" + principal.getPrincipalName());
		}

		return getRepository().getAssignableResources(
				realmService.getAssociatedPrincipals(principal));
	}
	
	protected void assertResourceAssignment(Principal principal, 
			Resource resource) throws AccessDeniedException {
		
		for(Resource r : getResources(principal)) {
			if(r.equals(resource)) {
				return;
			}
		}
		throw new AccessDeniedException("Principal " 
					+ principal.getName() 
					+ " does not have access to " 
					+ resource.getName());
	}
}
