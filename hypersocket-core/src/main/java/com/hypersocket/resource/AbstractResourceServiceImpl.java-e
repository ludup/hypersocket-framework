package com.hypersocket.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionService;

@Service
public abstract class AbstractResourceServiceImpl<T extends RealmResource>
		extends AbstractAuthenticatedServiceImpl implements AbstractResourceService<T>,
			EventPropertyCollector, AuthenticatedService {

	private static SecureRandom random = new SecureRandom();
	
	static Logger log = LoggerFactory
			.getLogger(AbstractAssignableResourceRepositoryImpl.class);

	static final String RESOURCE_BUNDLE_DEFAULT = "AssignableResourceService";

	final String resourceCategory;
	
	@Autowired
	RealmService realmService;

	boolean assertPermissions = true;

	String fingerprint;
	
	@Autowired
	TransactionService transactionService; 
	
	protected abstract AbstractResourceRepository<T> getRepository();

	protected abstract String getResourceBundle();

	public abstract Class<? extends PermissionType> getPermissionType();
	
	protected abstract Class<T> getResourceClass();
	
	protected AbstractResourceServiceImpl(String resourceCategory) {
		this.resourceCategory = resourceCategory;
	}
	
	@Override
	public void extendPropertyTemplates(String path) {
		getRepository().loadPropertyTemplates(path);
	}
	
	@Override
	public String getResourceCategory() {
		return resourceCategory;
	}
	
	protected PermissionType getUpdatePermission() {
		return getPermission("UPDATE");
	}

	protected PermissionType getCreatePermission() {
		return getPermission("CREATE");
	}

	protected PermissionType getDeletePermission() {
		return getPermission("DELETE");
	}

	protected PermissionType getUpdatePermission(T resource) {
		return getUpdatePermission();
	}

	protected PermissionType getCreatePermission(T resource) {
		return getCreatePermission();
	}

	protected PermissionType getDeletePermission(T resource) {
		return getDeletePermission();
	}
	
	protected PermissionType getReadPermission() {
		return getPermission("READ");
	}

	protected void setAssertPermissions(boolean assertPermissions) {
		this.assertPermissions = assertPermissions;
	}
	
	protected PermissionType getPermission(String name) {
		
		if(getPermissionType()==null) {
			return SystemPermission.SYSTEM_ADMINISTRATION;
		}
		
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
		return getRepository().getPropertyNames(null);
	}
	
	@Override
	@SafeVarargs
	public final void createResource(T resource, TransactionOperation<T>... ops) throws ResourceCreationException,
			AccessDeniedException {
		createResource(resource, new HashMap<String,String>(), ops);
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
	public final void createResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceCreationException,
			AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getCreatePermission(resource));
		}
		if(resource.getRealm()==null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
					"generic.create.error", "Calling method should set realm");
		}
		
		resource.setResourceCategory(resourceCategory);
		getRepository().populateEntityFields(resource, properties);
		
		if(!checkUnique(resource, true)) {
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE_DEFAULT, "generic.alreadyExists.error",
				resource.getName());
			fireResourceCreationEvent(resource, ex);
			throw ex;
		}
		
		try {
			beforeCreateResource(resource, properties);
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
				throw new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
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
	
	protected void beforeDeleteResource(T resource) throws ResourceChangeException {
		
	}
	
	protected void afterDeleteResource(T resource) throws ResourceChangeException {
		
	}
	
	protected abstract void fireResourceCreationEvent(T resource);

	protected abstract void fireResourceCreationEvent(T resource, Throwable t);

	@SafeVarargs
	public final void updateResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {
		
		if(assertPermissions) {
			assertPermission(getUpdatePermission(resource));
		}

		if(resource.getRealm()==null) {
			resource.setRealm(isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
		}
		
		if(!checkUnique(resource, false)) {
			ResourceChangeException ex = new ResourceChangeException(
					RESOURCE_BUNDLE_DEFAULT, "generic.alreadyExists.error",
				resource.getName());
			fireResourceUpdateEvent(resource, ex);
			throw ex;
		}
		
		resource.setResourceCategory(resourceCategory);
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
				throw new ResourceChangeException(RESOURCE_BUNDLE_DEFAULT,
						"generic.update.error", t.getMessage());
			}
		}
	}
	
	@Override
	@SafeVarargs
	public final void updateResource(T resource, TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {
		updateResource(resource, new HashMap<String,String>(), ops);
	}

	protected abstract void fireResourceUpdateEvent(T resource);

	protected abstract void fireResourceUpdateEvent(T resource, Throwable t);

	@SafeVarargs
	@Override
	public final void deleteResource(T resource, TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getDeletePermission(resource));
		}
		
		try {
			beforeDeleteResource(resource);
			getRepository().deleteResource(resource, ops);
			updateFingerprint();
			afterDeleteResource(resource);
			fireResourceDeletionEvent(resource);
		} catch (Throwable t) {
			fireResourceDeletionEvent(resource, t);
			if (t instanceof ResourceChangeException) {
				throw (ResourceChangeException) t;
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE_DEFAULT,
						"generic.delete.error", t.getMessage());
			}
		}

	}

	protected abstract void fireResourceDeletionEvent(T resource);

	protected abstract void fireResourceDeletionEvent(T resource, Throwable t);

	@Override
	public List<T> getResources(Realm realm) throws AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}
		return getRepository().getResources(isSystemResource() ? realmService.getSystemRealm() : realm);

	}

	@Override
	public List<T> searchResources(Realm realm, String searchColumn, String search, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		return getRepository().search(isSystemResource() ? realmService.getSystemRealm() : realm, searchColumn, search, start, length, sorting);
	}
	
	protected boolean isSystemResource() {
		return false;
	}

	@Override
	public long getResourceCount(Realm realm, String searchColumn, String search)
			throws AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		return getRepository().getResourceCount(isSystemResource() ? realmService.getSystemRealm() : realm, searchColumn, search);
	}

	@Override
	public List<T> allResources() {
		return getRepository().getResources(isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
	}

	@Override
	public T getResourceByName(String name) throws ResourceNotFoundException {
		T resource = getRepository().getResourceByName(name, isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
		if (resource == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE_DEFAULT,
					"error.invalidResourceName", name);
		}
		return resource;
	}
	
	@Override
	public T getResourceByName(String name, Realm realm) throws ResourceNotFoundException, AccessDeniedException {
		
		if(assertPermissions) {
			assertPermission(getReadPermission());
		}
		
		T resource = getRepository().getResourceByName(name, isSystemResource() ? realmService.getSystemRealm() : realm);
		if (resource == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE_DEFAULT,
					"error.invalidResourceName", name);
		}
		return resource;
	}

	@Override
	public T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException {
		
		if(assertPermissions) {
			assertPermission(getReadPermission());
		}
		
		T resource = getRepository().getResourceById(id);
		if (resource == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE_DEFAULT,
					"error.invalidResourceId", id);
		}
		
		
		return resource;
	}
	
	@Override
	public Collection<PropertyCategory> getResourceTemplate() {
		return getRepository().getPropertyCategories(null);
	}
	
	@Override
	public Collection<PropertyCategory> getResourceProperties(T resource) {
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
	public String exportResources(@SuppressWarnings("unchecked") T... resources) throws ResourceExportException {
		return exportResources(Arrays.asList(resources));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String exportResoure(Long id) throws ResourceNotFoundException,
			ResourceExportException, AccessDeniedException {
		final T resource = getResourceById(id);
		return exportResources(resource);
	}

	@Override
	public String exportAllResoures() throws ResourceExportException {
		List<T> list = allResources();
		return exportResources(list);
	}
	
	protected boolean isExportingAdditionalProperties() {
		return false;
	}
	
	protected void prepareExport(T resource) {
		if(isExportingAdditionalProperties()) {
			resource.setProperties(getRepository().getProperties(resource));
		}
	}
	
	protected void prepareImport(T resource, Realm realm) throws ResourceCreationException, AccessDeniedException {
		
	}
	
	@Override
	public String exportResources(Collection<T> resources) throws ResourceExportException {

		if(resources.isEmpty()) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.nothingToExport");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		
		try {
			for(T resource : resources) {
				prepareExport(resource);
				resource.setId(null);
				resource.setRealm(null);
			}

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources);
		} catch (JsonProcessingException e) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.exportError", e.getMessage());
		}
	}
	
	@Override
	public Collection<T> importResources(final String json, final Realm realm, final boolean dropCurrent) throws AccessDeniedException, ResourceException {
		
		return transactionService.doInTransaction(new TransactionCallback<Collection<T>>() {

			@Override
			public Collection<T> doInTransaction(TransactionStatus status) {
				
				try {
				
					if(dropCurrent) {
						for(T resource : getResources(isSystemResource() ? realmService.getSystemRealm() : realm)) {
							performImportDropResources(resource);
						}
					}
					ObjectMapper mapper = new ObjectMapper();
					
					Collection<T> resources = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, getResourceClass()));
					for(T resource : resources) {
						performImport(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
					}
					
					return resources;
				} catch(ResourceException e) { 
					throw new IllegalStateException(e);
				}catch (AccessDeniedException | IOException e) {
					throw new IllegalStateException(new ResourceImportException(RESOURCE_BUNDLE_DEFAULT, "error.importError", e.getMessage()));
				}			
			}
		});
		
	}

	@Override
	public final String getFingerprint() {
		return fingerprint;
	}
	
	protected void performImportDropResources(T resource) throws ResourceChangeException, AccessDeniedException {
		deleteResource(resource);
	}
	
	protected void performImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		resource.setRealm(realm);
		checkImportName(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
		createResource(resource, resource.getProperties()==null ? new HashMap<String,String>() : resource.getProperties());
	}
	
	protected void checkImportName(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		
		try {
			prepareImport(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
			getResourceByName(resource.getName(), isSystemResource() ? realmService.getSystemRealm() : realm);
			resource.setName(resource.getName() + " [imported]");
		} catch(ResourceNotFoundException e) {
			return;
		}
	}
	
	protected final void updateFingerprint() {
		fingerprint = new BigInteger(130, random).toString(32);
	}
	
}
