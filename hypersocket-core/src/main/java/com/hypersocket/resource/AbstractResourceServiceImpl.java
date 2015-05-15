package com.hypersocket.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionService;

@Repository
public abstract class AbstractResourceServiceImpl<T extends RealmResource>
		extends AbstractAuthenticatedServiceImpl implements AbstractResourceService<T>,
			EventPropertyCollector {

	static Logger log = LoggerFactory
			.getLogger(AbstractAssignableResourceRepositoryImpl.class);

	static final String RESOURCE_BUNDLE = "AssignableResourceService";

	final String resourceCategory;
	
	@Autowired
	RealmService realm;

	boolean assertPermissions = true;
	
	@Autowired
	EntityResourcePropertyStore entityPropertyStore;
	
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

	protected PermissionType getReadPermission() {
		return getPermission("READ");
	}

	protected void setAssertPermissions(boolean assertPermissions) {
		this.assertPermissions = assertPermissions;
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
	public final void createResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceCreationException,
			AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getCreatePermission());
		}
		if(resource.getRealm()==null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"generic.create.error", "Calling method should set realm");
		}
		try {
			getResourceByName(resource.getName(), resource.getRealm());
			ResourceCreationException ex = new ResourceCreationException(
					RESOURCE_BUNDLE, "generic.alreadyExists.error",
					resource.getName());
			fireResourceCreationEvent(resource, ex);
			throw ex;
		} catch (ResourceNotFoundException ex) {
			try {
				resource.setResourceCategory(resourceCategory);
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
			assertPermission(getUpdatePermission());
		}

		try {
			if(resource.getRealm()==null) {
				resource.setRealm(getCurrentRealm());
			}
			resource.setResourceCategory(resourceCategory);
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

		if(assertPermissions) {
			assertPermission(getDeletePermission());
		}
		
		try {
			beforeDeleteResource(resource);
			getRepository().deleteResource(resource);
			afterDeleteResource(resource);
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

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}
		return getRepository().getResources(realm);

	}

	@Override
	public List<T> searchResources(Realm realm, String search, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		return getRepository().search(realm, search, start, length, sorting);
	}

	@Override
	public long getResourceCount(Realm realm, String search)
			throws AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		return getRepository().getResourceCount(realm, search);
	}

	@Override
	public List<T> getResources() {
		return getRepository().getResources(getCurrentRealm());
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
	public T getResourceByName(String name, Realm realm) throws ResourceNotFoundException {
		T resource = getRepository().getResourceByName(name, realm);
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
	public String exportResources(@SuppressWarnings("unchecked") T... resources) throws ResourceExportException {
		return exportResources(Arrays.asList(resources));
	}
	
	@Override
	public String exportResources(Collection<T> resources) throws ResourceExportException {

		if(resources.isEmpty()) {
			throw new ResourceExportException(RESOURCE_BUNDLE, "error.nothingToExport");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			for(T resource : resources) {
				resource.setId(null);
				resource.setRealm(null);
			}

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources);
		} catch (JsonProcessingException e) {
			throw new ResourceExportException(RESOURCE_BUNDLE, "error.exportError", e.getMessage());
		}
	}
	
	@Override
	public Collection<T> importResources(final String json, final Realm realm) throws AccessDeniedException, ResourceException {
		
		return transactionService.doInTransaction(new TransactionCallback<Collection<T>>() {

			@Override
			public Collection<T> doInTransaction(TransactionStatus status) {
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					Collection<T> resources = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, getResourceClass()));
					for(T resource : resources) {
						resource.setRealm(realm);
						checkImportName(resource, realm);
						createResource(resource, new HashMap<String,String>());
					}
					
					return resources;
				} catch(ResourceCreationException e) { 
					throw new IllegalStateException(e);
				}catch (AccessDeniedException | IOException e) {
					throw new IllegalStateException(new ResourceImportException(RESOURCE_BUNDLE, "error.importError", e.getMessage()));
				}			
			}
		});
		
	}
	protected void checkImportName(T resource, Realm realm) {
		
		try {
			getResourceByName(resource.getName(), realm);
			resource.setName(resource.getName() + " [imported]");
		} catch(ResourceNotFoundException e) {
			return;
		}
	}
	
}
