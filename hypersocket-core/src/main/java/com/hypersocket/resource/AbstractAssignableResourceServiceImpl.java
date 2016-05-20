package com.hypersocket.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionService;

@Service
public abstract class AbstractAssignableResourceServiceImpl<T extends AssignableResource>
		extends PasswordEnabledAuthenticatedServiceImpl implements
		AbstractAssignableResourceService<T> {

	private static SecureRandom random = new SecureRandom();

	static Logger log = LoggerFactory
			.getLogger(AbstractAssignableResourceRepositoryImpl.class);

	static final String RESOURCE_BUNDLE = "AssignableResourceService";

	@Autowired
	RealmService realmService;

	@Autowired
	TransactionService transactionService; 
	
	String resourceCategory;

	private String fingerprint;
	
	protected abstract AbstractAssignableResourceRepository<T> getRepository();

	protected abstract String getResourceBundle();
	
	public abstract Class<?> getPermissionType();

	protected abstract Class<T> getResourceClass();
	
	protected AbstractAssignableResourceServiceImpl(String resourceCateogory) {
		this.resourceCategory = resourceCateogory;
	}
	
	protected PermissionType getUpdatePermission() {
		return getPermission("UPDATE");
	}

	protected PermissionType getUpdatePermission(T resource) {
		return getUpdatePermission();
	}
	
	protected PermissionType getCreatePermission() {
		return getPermission("CREATE");
	}

	protected PermissionType getCreatePermission(T resource) {
		return getCreatePermission();
	}
	
	protected PermissionType getDeletePermission() {
		return getPermission("DELETE");
	}
	
	protected PermissionType getDeletePermission(T resource) {
		return getDeletePermission();
	}

	protected PermissionType getReadPermission() {
		return getPermission("READ");
	}

	@Override
	public String getFingerprint() {
		return fingerprint;
	}

	@Override
	public String getResourceCategory() {
		return resourceCategory;
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
	
	protected boolean checkUnique(T resource, boolean create) throws AccessDeniedException {
		try {
			if(!create) {
				if(!resource.hasNameChanged()) {
					return true;
				}
			}
			getResourceByName(resource.getName(), resource.getRealm());
			return false;
		} catch (ResourceNotFoundException e) {
			return true;
		}
	}
	
	@Override
	@SafeVarargs
	public final void createResource(T resource, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceCreationException,
			AccessDeniedException {

		assertPermission(getCreatePermission(resource));

		if(resource.getRealm()==null) {
			resource.setRealm(getCurrentRealm());
		}
		resource.setResourceCategory(resourceCategory);
		getRepository().populateEntityFields(resource, properties);
		
		try {
		
			beforeCreateResource(resource, properties);
			
			if(!checkUnique(resource, true)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"generic.alreadyExists.error", resource.getName());
			}
			
			getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			
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
	@Override
	public final void updateResource(T resource,  
			TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {
		updateResource(resource, new HashMap<String,String>(), ops);
	}
	
	protected boolean isAssignedUserAllowedUpdate() {
		return false;
	}
	
	@SafeVarargs
	@Override
	public final void updateResource(T resource, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {
		
		if(isAssignedUserAllowedUpdate()) {
			assertPrincipalAssignment(resource, getUpdatePermission());
		} else {
			assertPermission(getUpdatePermission(resource));
		}
		

		if(!checkUnique(resource, false)) {
			ResourceChangeException ex = new ResourceChangeException(RESOURCE_BUNDLE,
					"generic.alreadyExists.error", resource.getName());
			fireResourceCreationEvent(resource, ex);
			throw ex;
		}

		
		if(resource.getRealm()==null) {
			resource.setRealm(getCurrentRealm());
		}
		getRepository().populateEntityFields(resource, properties);
		
		
		try {

			beforeUpdateResource(resource, properties);

			getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			

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
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {

		assertPermission(getDeletePermission(resource));

		try {
			getRepository().deleteResource(resource, ops);
			updateFingerprint();
			
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
	public Collection<T> searchPersonalResources(Principal principal, String searchColumn, String search, int start,
			int length, ColumnSort[] sorting) {

		List<Principal> principals = realmService.getAssociatedPrincipals(principal);
		return getRepository().searchAssignedResources(principals, search, start, length, sorting);
	}
	
	@Override
	public Collection<T> getPersonalRoleResources(Principal principal) {
		return getRepository().getAssignedResources(permissionService.getPersonalRole(principal));
	}
	
	@Override
	public Collection<T> getPersonalResources(Principal principal) {
		return getRepository().getAssignedResources(realmService.getAssociatedPrincipals(principal));
	}
	
	@Override
	public Collection<T> getPersonalResources(Principal principal, boolean resolveAssosicatedPrincipals) {
		if(resolveAssosicatedPrincipals) {
			return getRepository().getAssignedResources(realmService.getAssociatedPrincipals(principal));
		} else {
			return getRepository().getAssignedResources(Arrays.asList(principal));
		}
		
	}

	@Override
	public Collection<T> getPersonalResources() {
		return getRepository().getAssignedResources(realmService.getAssociatedPrincipals(getCurrentPrincipal()));
	}
	
	@Override
	public long getPersonalResourceCount(Principal principal, String searchColumn, String search) {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(principal), search);
	}
	
	@Override
	public long getPersonalResourceCount(String search) {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(getCurrentPrincipal()), search);
	}

	@Override
	public long getPersonalResourceCount() {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(getCurrentPrincipal()), "");
	}
	
	@Override
	public List<T> searchResources(Realm realm, String searchColumn, String search, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().search(realm, searchColumn, search, start, length, sorting);
	}

	@Override
	public long getResourceCount(Realm realm, String searchColumn, String search)
			throws AccessDeniedException {

		assertPermission(getReadPermission());

		return getRepository().getResourceCount(realm, searchColumn, search);
	}

	@Override
	public T getResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException {
		
		
		T resource = getRepository().getResourceByName(name, getCurrentRealm());
		
		if (resource == null) {
			assertPermission(getReadPermission());

			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}

		assertPrincipalAssignment(resource, getReadPermission());
		
		return resource;
	}
	
	@Override
	public T getResourceByName(String name, Realm realm) throws ResourceNotFoundException, AccessDeniedException {
		
		T resource = getRepository().getResourceByName(name, realm);
		
		if (resource == null) {
			assertPermission(getReadPermission());
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		
		assertPrincipalAssignment(resource, getReadPermission());
		
		return resource;
	}

	protected void assertPrincipalAssignment(T resource, PermissionType permission) throws AccessDeniedException {
		if(Collections.disjoint(resource.getRoles(), 
				permissionService.getPrincipalRoles(getCurrentPrincipal()))) {
			assertPermission(permission);
		}
	}
	
	@Override
	public T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException {
		
		T resource = getRepository().getResourceById(id);
		if (resource == null) {
			assertPermission(getReadPermission());
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceId", id);
		}
		
		assertPrincipalAssignment(resource, getReadPermission());
		
		return resource;
	}

	@Override
	public Collection<T> getResources(Principal principal)
			throws AccessDeniedException {

		if (log.isDebugEnabled()) {
			log.debug("Looking up resources for principal "
					+ principal.getRealm().getName() + "/" + principal.getPrincipalName());
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
	
	@SuppressWarnings("unchecked")
	@Override
	public String exportResoure(Long id) throws ResourceNotFoundException,
			ResourceExportException, AccessDeniedException {
		final T resource = getResourceById(id);
		return exportResources(true, resource);
	}

	@Override
	public String exportAllResoures() throws ResourceExportException {
		List<T> list = getResources();
		return exportResources(list, true);
	}
	
	@Override
	public String exportResources(boolean stripIdentity, @SuppressWarnings("unchecked") T... resources) throws ResourceExportException {
		return exportResources(Arrays.asList(resources), stripIdentity);
	}
	
	protected boolean isExportingAdditionalProperties() {
		return false;
	}
	
	protected void prepareExport(T resource) {
		prepareExport(resource, true);
	}
	
	protected void prepareExport(T resource, boolean stripIdentity) {
		if(isExportingAdditionalProperties()) {
			resource.setProperties(getRepository().getProperties(resource));
		}
	}
	
	protected void prepareImport(T resource, Realm realm) throws ResourceCreationException, AccessDeniedException {
		
	}
	
	@Override
	public String exportResources(Collection<T> resources, boolean stripIdentity) throws ResourceExportException {

		if(resources.isEmpty()) {
			throw new ResourceExportException(RESOURCE_BUNDLE, "error.nothingToExport");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			for(T resource : resources) {
				prepareExport(resource, stripIdentity);
				if(stripIdentity) {
					resource.setId(null);
					resource.setRealm(null);
					resource.getRoles().clear();
				}
			}

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources);
		} catch (JsonProcessingException e) {
			throw new ResourceExportException(RESOURCE_BUNDLE, "error.exportError", e.getMessage());
		}
	}
	
	@Override
	public Collection<T> importResources(final String json, final Realm realm, final boolean dropCurrent) throws AccessDeniedException, ResourceException {
		
		return transactionService.doInTransaction(new TransactionCallback<Collection<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Collection<T> doInTransaction(TransactionStatus status) {
				
				try {
					
					if(dropCurrent) {
						for(T resource : getResources(realm)) {
							deleteResource(resource);
						}
					}
					ObjectMapper mapper = new ObjectMapper();
					
					Collection<T> resources = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, getResourceClass()));
					for(T resource : resources) {
						performImport(resource, realm);
					}
					
					return resources;
				} catch(ResourceException e) { 
					throw new IllegalStateException(e);
				}catch (AccessDeniedException | IOException e) {
					throw new IllegalStateException(new ResourceImportException(RESOURCE_BUNDLE, "error.importError", e.getMessage()));
				}			
			}
		});
		
	}
	
	protected void performImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		resource.setRealm(realm);
		checkImportName(resource, realm);
		createResource(resource, resource.getProperties()==null ? new HashMap<String,String>() : resource.getProperties());
	}
	
	protected void checkImportName(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		
		try {
			prepareImport(resource, realm);
			getResourceByName(resource.getName(), realm);
			resource.setName(resource.getName() + " [imported]");
		} catch(ResourceNotFoundException e) {
			return;
		}
	}
	
	
	@Override
	public long getPrincipalsInUse(Realm realm) {
		
		if(getRepository().hasAssignedEveryoneRole(realm)) {
			return realmService.getPrincipalCount(realm);
		} else {
			Set<Principal> tmp = new HashSet<Principal>();
			
			for(Principal p : getRepository().getAssignedPrincipals(realm)) {
				if(p.getType()==PrincipalType.USER) {
					tmp.add(p);
				} else if(p.getType()==PrincipalType.GROUP) {
					tmp.addAll(realmService.getAssociatedPrincipals(p, PrincipalType.USER));
				}
			}
			
			return tmp.size();
		}
	}
	
	@Override
	public void saveMetaData(T resource, String key, String value) throws AccessDeniedException {
		
		assertAnyPermission(getUpdatePermission());
		
		getRepository().setValue(resource, key, value);
	}
	
	@Override
	public String getMetaData(T resource, String key, String defaultValue) throws AccessDeniedException {
		
		assertAnyPermission(getReadPermission());
		return getRepository().getValue(resource, key, defaultValue);
	}
	
	@Override
	public Collection<PropertyCategory> getResourceTemplate() {
		return getRepository().getPropertyCategories(null);
	}
	
	@Override
	public Collection<PropertyCategory> getResourceProperties(T resource) throws AccessDeniedException {
		return getRepository().getPropertyCategories(resource);
	}
	
	@Override
	public String getResourceProperty(T resource, String resourceKey) {
		return getRepository().getValue(resource, resourceKey);
	}
	
	@Override
	public boolean getResourceBooleanProperty(T resource, String resourceKey) {
		return getRepository().getBooleanValue(resource, resourceKey);
	}
	
	@Override
	public int getResourceIntProperty(T resource, String resourceKey) {
		return getRepository().getIntValue(resource, resourceKey);
	}

	@Override
	public Long getResourceLongProperty(T resource, String resourceKey) {
		return getRepository().getLongValue(resource, resourceKey);
	}
	protected void updateFingerprint() {
		fingerprint = new BigInteger(130, random).toString(32);
	}
}
