package com.hypersocket.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.bulk.BulkAssignment;
import com.hypersocket.bulk.BulkAssignmentEvent;
import com.hypersocket.bulk.BulkAssignmentMode;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.*;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

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

	@Autowired
	EventService eventService;
	
	String resourceCategory;

	private String fingerprint;

	@PostConstruct
	private void postConstruct() {
		eventService.registerEvent(BulkAssignmentEvent.class, RESOURCE_BUNDLE);
		permissionService.registerAssignableRepository(getResourceClass(), getRepository());
	}
	
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
			
			T existing = getResourceByName(resource.getName(), resource.getRealm());
			
			if(resource.getPersonal()) {
				existing = getPersonalResourceByName(resource.getName());
			} 
			return existing.getId().equals(resource.getId());
		} catch (ResourceNotFoundException e) {
			return true;
		}
	}
	
	protected ResourceException createDuplicateException(T resource) {
		return new ResourceCreationException(RESOURCE_BUNDLE,
				"generic.alreadyExists.error", resource.getName());
	}
	
	@SafeVarargs
	protected final void createPersonalResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceException, AccessDeniedException {
		resource.setPersonal(true);
		resource.getRoles().clear();
		resource.getRoles().add(permissionService.getPersonalRole(getCurrentPrincipal()));
		createResource(resource, properties, ops);
	}
	
	@Override
	@SafeVarargs
	public final void createResource(T resource, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {

		assertPermission(getCreatePermission(resource));

		if(resource.getRealm()==null) {
			resource.setRealm(getCurrentRealm());
		}
		resource.setResourceCategory(resourceCategory);
		
		try {
		
			beforeCreateResource(resource, properties);
			
			if(!checkUnique(resource, true)) {
				ResourceException ex = createDuplicateException(resource);
				fireResourceCreationEvent(resource, ex);
				throw ex;
			}
			
			resource.setAssignedRoles(resource.getRoles());
			
			getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			
			afterCreateResource(resource, properties);

			fireResourceCreationEvent(resource);
		} catch (Throwable t) {
			if(t instanceof ResourceConfirmationException)  {
				throw t;
			}
			if(t.getCause() instanceof ResourceConfirmationException) {
				throw (ResourceConfirmationException)t.getCause();
			}
			log.error("Failed to create resource", t);
			fireResourceCreationEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else if(t.getCause() instanceof ResourceException) {
				throw (ResourceException)t.getCause();
			} else  {
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"generic.create.error", t.getMessage());
			}
		}
	

	}

	protected void beforeCreateResource(T resource, Map<String,String> properties) throws ResourceException {
		
	}

	protected void afterCreateResource(T resource, Map<String,String> properties) throws  ResourceException {
		
	}
	
	protected void beforeUpdateResource(T resource, Map<String,String> properties) throws ResourceException {
		
	}
	
	protected void afterUpdateResource(T resource, Map<String,String> properties) throws ResourceException {
		
	}
	
	protected abstract void fireResourceCreationEvent(T resource);

	protected abstract void fireResourceCreationEvent(T resource, Throwable t);

	@SafeVarargs
	@Override
	public final void updateResource(T resource,  
			TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {
		updateResource(resource, null, new HashMap<String,String>(), ops);
	}
	
	protected boolean isAssignedUserAllowedUpdate() {
		return false;
	}
	
	@SafeVarargs
	@Override
	public final void updateResource(T resource, Set<Role> roles, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {
		
		if(isAssignedUserAllowedUpdate()) {
			assertPrincipalAssignment(resource, getUpdatePermission());
		} else {
			assertPermission(getUpdatePermission(resource));
		}
		
		if(resource.getRealm()==null) {
			resource.setRealm(getCurrentRealm());
		}
		
		Set<Role> assigned = new HashSet<Role>();
		if(roles!=null) {
			assigned.addAll(roles);
			assigned.removeAll(resource.getRoles());
		}
		
		Set<Role> unassigned = new HashSet<Role>();
		if(roles!=null) {
			unassigned.addAll(resource.getRoles());
			unassigned.removeAll(roles);
		}
		
		if(roles!=null) {
			resource.getRoles().clear();
			resource.getRoles().addAll(roles);
		}
		
		resource.setAssignedRoles(assigned);
		resource.setUnassignedRoles(unassigned);

		if(!checkUnique(resource, false)) {
			ResourceException ex = createDuplicateException(resource);
			fireResourceUpdateEvent(resource, ex);
			throw new ResourceChangeException(ex);
		}

		try {

			beforeUpdateResource(resource, properties);

			getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			

			afterUpdateResource(resource, properties);

			fireResourceUpdateEvent(resource);
		} catch (Throwable t) {
			if(t instanceof ResourceConfirmationException)  {
				throw t;
			}
			if(t.getCause() instanceof ResourceConfirmationException) {
				throw (ResourceConfirmationException)t.getCause();
			}
			fireResourceUpdateEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else if (t.getCause() instanceof ResourceException) {
				throw (ResourceException) t.getCause();
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"generic.update.error", t.getMessage());
			}
		}
	}

	
	protected abstract void fireResourceUpdateEvent(T resource);
	
	protected abstract void fireResourceUpdateEvent(T resource, Throwable t);

	@SuppressWarnings("unchecked")
	@Override 
	public void deleteResource(Long id) throws ResourceException, AccessDeniedException {
		deleteResource(getResourceById(id));
	}
	
	@Override
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws 
			AccessDeniedException, ResourceException {

		assertPermission(getDeletePermission(resource));

		try {
			resource.setUnassignedRoles(resource.getRoles());
			
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
		return getRepository().searchAssignedResources(principals, search, searchColumn, start, length, sorting);
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
	public long getPersonalResourceCount(Principal principal) {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(principal), "", "");
	}
	
	@Override
	public long getPersonalResourceCount(Principal principal, String searchColumn, String search) {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(principal), search, searchColumn);
	}
	
	@Override
	public long getPersonalResourceCount(String search) {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(getCurrentPrincipal()), search, "");
	}

	@Override
	public long getPersonalResourceCount() {
		return getRepository().getAssignedResourceCount(realmService.getAssociatedPrincipals(getCurrentPrincipal()), "", "");
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
	public T getAssignedResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException {
		return getAssignedResourceByName(name, getCurrentPrincipal());
	}
	
	public T getAssignedResourceByName(String name, Principal principal) throws ResourceNotFoundException, AccessDeniedException {
		
		if(StringUtils.isBlank(name)) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		
		Collection<T> resources = getRepository().getAssignedResources(name, realmService.getAssociatedPrincipals(principal));
		if(resources.isEmpty()) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		return resources.iterator().next();
	}
	
	@Override
	public T getPersonalResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException {
		return getPersonalResourceByName(name, getCurrentPrincipal());
	}
	
	@Override
	public T getPersonalResourceByName(String name, Principal principal) throws ResourceNotFoundException, AccessDeniedException {
		
		T resource = getRepository().getPersonalResourceByName(name, principal);
		if (resource == null) {
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		
		return resource;
	}

	protected void assertPrincipalAssignment(T resource, PermissionType permission) throws AccessDeniedException {
		if(Collections.disjoint(getPersonalResources(), Arrays.asList(resource))) {
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
			Resource resource, boolean allowSystem) throws AccessDeniedException {

		if(allowSystem) {
			if(permissionService.hasSystemPermission(getCurrentPrincipal())) {
				return;
			}
		}
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
					throw new IllegalStateException(e.getMessage(), e);
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

	@Override
	public void bulkAssignRolesToResource(final BulkAssignment bulkAssignment)
			throws ResourceException {
		final List<Long> roleIds = bulkAssignment.getRoleIds();
		final List<Long> resourceIds = bulkAssignment.getResourceIds();
		final BulkAssignmentMode bulkAssignmentMode = bulkAssignment.getMode();

		if(roleIds == null || roleIds.isEmpty()) {
			throw new IllegalArgumentException("Roles cannot be empty.");
		}

		if(resourceIds == null || resourceIds.isEmpty()) {
			throw new IllegalArgumentException("Resources cannot be empty.");
		}


		transactionService.doInTransaction(new TransactionCallbackWithError<Void>() {

			Principal principal = realmService.getCurrentPrincipal();
			Realm realm = realmService.getCurrentRealm();
			final Session session = realmService.getCurrentSession();
			final RealmProvider realmProvider = realmService.getProviderForRealm(realm);

			@Override
			public Void doInTransaction(TransactionStatus transactionStatus) {

				getRepository().bulkAssignRolesToResource(bulkAssignment);
				eventService.publishEvent(new BulkAssignmentEvent(this, roleIds, resourceIds, bulkAssignmentMode,
						session, realm,
						realmProvider, principal));
				return null;
			}

			@Override
			public void doTransacationError(Throwable e) {
				eventService.publishEvent(new BulkAssignmentEvent(this, roleIds, resourceIds, bulkAssignmentMode,
						e, session, realm,
						realmProvider, principal));
			}
		});
	}

	protected void updateFingerprint() {
		fingerprint = new BigInteger(130, random).toString(32);
	}
}
