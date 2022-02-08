package com.hypersocket.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

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
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;

@Service
public abstract class AbstractAssignableResourceServiceImpl<T extends AssignableResource>
		extends PasswordEnabledAuthenticatedServiceImpl implements
		AbstractAssignableResourceService<T> {

	private static SecureRandom random = new SecureRandom();

	static Logger log = LoggerFactory
			.getLogger(AbstractAssignableResourceRepositoryImpl.class);

	static final String RESOURCE_BUNDLE_DEFAULT = "AssignableResourceService";

	@Autowired
	private RealmService realmService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EventService eventService;
	
	private String resourceCategory;
	private String fingerprint;

	@PostConstruct
	private void postConstruct() {
		eventService.registerEvent(BulkAssignmentEvent.class, RESOURCE_BUNDLE_DEFAULT);
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
			
			T existing;
			
			if(resource.getPersonal()) {
				existing = getPersonalResourceByName(resource.getName());
			} else {
				 existing = getResourceByName(resource.getName(), resource.getRealm());
			}
			
			return existing.getId().equals(resource.getId());
		} catch (ResourceNotFoundException e) {
			return true;
		}
	}
	
	protected ResourceException createDuplicateException(T resource) {
		return new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
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
		
			if(!checkUnique(resource, true)) {
				ResourceException ex = createDuplicateException(resource);
				fireResourceCreationEvent(resource, ex);
				throw ex;
			}
			
			resource.setAssignedRoles(resource.getRoles());
			
			beforeCreateResource(resource, properties);
			
			getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			
			afterCreateResource(resource, properties);

			fireResourceCreationEvent(resource);
		} catch (Throwable t) {
			ResourcePassthroughException.maybeRethrow(t);
			log.error("Failed to create resource", t);
			fireResourceCreationEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else if(t.getCause() instanceof ResourceException) {
				throw (ResourceException)t.getCause();
			} else  {
				ResourceCreationException e = new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
						"generic.create.error", t.getMessage());
				e.initCause(t);
				throw e;
			}
		}
	

	}

	protected void beforeCreateResource(T resource, Map<String,String> properties) throws AccessDeniedException, ResourceException {
		
	}

	protected void afterCreateResource(T resource, Map<String,String> properties) throws  AccessDeniedException, ResourceException {
		
	}
	
	protected void beforeUpdateResource(T resource, Map<String,String> properties) throws AccessDeniedException, ResourceException {
		
	}
	
	protected void afterUpdateResource(T resource, Map<String,String> properties) throws AccessDeniedException, ResourceException {
		
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
	
	protected Long getResourceCount(Realm realm) {
		return getRepository().getResourceCount(realm);
	}
	
	@SafeVarargs
	@Override
	public final void updateResource(T resource, Set<Role> roles, Map<String,String> properties,  
			TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {
		
		boolean changedDefault = false;
		
		if(isAssignedUserAllowedUpdate()) {
			assertPrincipalAssignment(resource, getUpdatePermission());
		} else {
			assertPermission(getUpdatePermission(resource));
		}
		
		if(resource.getRealm()==null) {
			resource.setRealm(getCurrentRealm());
		}
		
		Set<Role> assigned = new HashSet<>();
		if(roles!=null) {
			assigned.addAll(roles);
			assigned.removeAll(resource.getRoles());
		}
		
		Set<Role> unassigned = new HashSet<>();
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

		if(resource.getRealm()==null) {
			resource.setRealm(isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
			changedDefault = true;
		}
		
		if(!checkUnique(resource, false)) {
			ResourceException ex = createDuplicateException(resource);
			fireResourceUpdateEvent(resource, ex);
			throw ex;
		}
		
		if(!Objects.equals(resource.getResourceCategory(), resourceCategory)) {
			resource.setResourceCategory(resourceCategory);
			changedDefault = true;
		}
		
		try {
			beforeUpdateResource(resource, properties);
			List<PropertyChange> changes = getRepository().saveResource(resource, properties, ops);
			updateFingerprint();
			afterUpdateResource(resource, properties);
			if(changes.size() > 0) {
				changedDefault = changedDefault || fireNonStandardEvents(resource, changes);
			}
			else {
				changedDefault = true;
			}	
			if(changedDefault) {
				fireResourceUpdateEvent(resource);
			}

		} catch (Throwable t) {
			ResourcePassthroughException.maybeRethrow(t);
			fireResourceUpdateEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else if (t.getCause() instanceof ResourceException) {
				throw (ResourceException) t.getCause();
			} else {
				throw new ResourceChangeException(t, RESOURCE_BUNDLE_DEFAULT,
						"generic.update.error", t.getMessage());
			}
		}
	}

	protected List<PropertyChange> populateEntityFields(T resource, Map<String,String> properties) {
		return getRepository().populateEntityFields(resource, properties);
	}
	
	protected boolean isSystemResource() {
		return false;
	}
	
	protected abstract void fireResourceUpdateEvent(T resource);
	
	protected abstract void fireResourceUpdateEvent(T resource, Throwable t);

	@SuppressWarnings("unchecked")
	@Override 
	public void deleteResource(Long id) throws ResourceException, AccessDeniedException {
		deleteResource(getResourceById(id));
	}
	
	/**
	 * 
	 * If the resource wants to fire particular events for particular property changes,
	 * then it should override this method. The list of resourceKey's for the properties
	 * will be  supplied, and a boolean indicating whether the default resource change
	 * event should be fired.
	 * 
	 * @param changes list of resourceKey changes
	 * @param resource resource being updated
	 * @return fire default resource change event
	 */
	protected boolean fireNonStandardEvents(T resource, List<PropertyChange> changes) {
		return true;
	}
	
	@Override
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws 
			AccessDeniedException, ResourceException {

		if(!resource.getPersonal()) {
			assertPermission(getDeletePermission(resource));
		} else {
			assertRoleOrAnyPermission(permissionService.getPersonalRole(getCurrentPrincipal()));
		}

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
				throw new ResourceChangeException(t, RESOURCE_BUNDLE_DEFAULT,
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
	
	@Override
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
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);
		}
		
		assertPrincipalAssignment(resource, getReadPermission());
		
		return resource;
	}

	@Override
	public T getPersonalResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException {
		return getPersonalResourceByName(name, getCurrentPrincipal());
	}
	
	@Override
	public T getPersonalResourceByName(String name, Principal principal) throws ResourceNotFoundException, AccessDeniedException {
		
		for(T resource : getPersonalResources()) {
			if(resource.getName().equalsIgnoreCase(name)) {
				return resource;
			}
		}

		throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceName", name);

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
			throw new ResourceNotFoundException(getResourceBundle(),
					"error.invalidResourceId", id);
		}
		
		assertPrincipalAssignment(resource, getReadPermission());
		
		return resource;
	}
	
	@Override
	public T getResourceByLegacyId(Long id) throws ResourceNotFoundException, AccessDeniedException {
		
		T resource = getRepository().getResourceByLegacyId(id);
		if (resource == null) {
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
	
	protected boolean prepareExport(T resource) {
		return prepareExport(resource, true);
	}
	
	protected boolean prepareExport(T resource, boolean stripIdentity) {
		if(isExportingAdditionalProperties()) {
			resource.setProperties(getRepository().getProperties(resource));
		}
		return true;
	}
	
	protected boolean prepareImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Preparing import %s", resource.getName()));
		}
		
		resource.preserveTimestamp();
		resource.setRealm(realm);
		return true;
	}
	
	@Override
	public String exportResources(Collection<T> resources, boolean stripIdentity) throws ResourceExportException {

		if(resources.isEmpty()) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.nothingToExport");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		List<T> exported = new  ArrayList<T>();
		
		try {
			for(T resource : resources) {
				if(prepareExport(resource, stripIdentity)) {
					if(stripIdentity) {
						resource.setLegacyId(resource.getId());
						resource.setId(null);
						resource.setRealm(null);
						resource.getRoles().clear();
					}
					exported.add(resource);
				}
			}

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exported);
		} catch (JsonProcessingException e) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.exportError", e.getMessage());
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
							if(!Objects.isNull(resource.getLegacyId())) {
								deleteResource(resource);
							}
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
					ResourceImportException ex = new ResourceImportException(RESOURCE_BUNDLE_DEFAULT, "error.importError", e.getMessage());
					throw new IllegalStateException(ex.getMessage(), ex);
				}			
			}
		});
		
	}
	
	protected void performImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		resource.setRealm(realm);
		T existing = null;
		try {
			existing = getResourceByLegacyId(resource.getLegacyId());
		} catch(ResourceNotFoundException ex) { }
		
		if(Objects.isNull(existing)) {
			if(prepareImport(resource, realm)) {
				if(checkImportName(resource, realm)) {
					createResource(resource, resource.getProperties()==null ? new HashMap<String,String>() : resource.getProperties());
					onResourceImported(resource);
				}
			}
		}
	}
	
	protected void onResourceImported(T resource) {
		
	}

	protected boolean checkImportName(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		
		try {
			
			getResourceByName(resource.getName(), realm);
			resource.setName(resource.getName() + " [imported]");
		} catch(ResourceNotFoundException e) {
		}
		
		return true;
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
	public void setResourceProperty(T resource, String resourceKey, String value) {
		getRepository().setValue(resource, resourceKey, value);
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
	
	@Override
	public void deleteResources(final List<T> resources, 
			@SuppressWarnings("unchecked") final TransactionOperation<T>... ops) 
			throws ResourceException, AccessDeniedException {
		
		assertPermission(getDeletePermission());
		
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				try {
					getRepository().deleteResources(resources, ops);
				} catch (ResourceException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				return null;
			}
		});
		
	}
	
	@Override
	public List<T> getResourcesByIds(Long...ids) throws AccessDeniedException {
		assertPermission(getReadPermission());
		
		return getRepository().getResourcesByIds(ids);
	}
}
