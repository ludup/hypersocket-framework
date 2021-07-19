package com.hypersocket.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.hypersocket.properties.ResourceUtils;
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

	public static final String RESOURCE_BUNDLE_DEFAULT = "AssignableResourceService";

	protected final String resourceCategory;

	@Autowired
	protected RealmService realmService;

	@Autowired
	private TransactionService transactionService;

	protected boolean assertPermissions = true;

	private String fingerprint;

	protected abstract AbstractResourceRepository<T> getRepository();

	protected abstract String getResourceBundle();

	public abstract Class<? extends PermissionType> getPermissionType();

	protected abstract Class<T> getResourceClass();

	protected AbstractResourceServiceImpl(String resourceCategory) {
		this.resourceCategory = resourceCategory;
		updateFingerprint();
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
	
	protected Long getResourceCount(Realm realm) {
		return getRepository().getResourceCount(realm);
	}

	@Override
	@SafeVarargs
	public final void createResource(T resource, TransactionOperation<T>... ops) throws ResourceException,
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
			T existing = getResourceByName(resource.getName(), resource.getRealm());
			return existing.getId().equals(resource.getId());
		} catch (ResourceNotFoundException e) {
			return true;
		}
	}

	@Override
	@SafeVarargs
	public final void createResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {

		processName(resource, properties);
		
		if(assertPermissions) {
			assertPermission(getCreatePermission(resource));
		}
		
		if(resource.getRealm()==null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
					"generic.create.error", "Calling method should set realm");
		}

		resource.setResourceCategory(resourceCategory);
		
		/* Note that a copy of 'properties' is supplied here, as populateEntityFields
		 * REMOVES properties from this map. So the 2nd time this is run, values
		 * will be set to null. This is because all property templates are iterated
		 * over and the value retrieve from this map. 
		 * 
		 * Before #M167, setting null on an entity was not possible. It now is, but that exposed this slightly odd behaviour (which
		 * although didn't have any ill effects, it was running a lot of code pointlessly).  
		 * 
		 */
		getRepository().populateEntityFields(resource, properties == null ? Collections.emptyMap() : new HashMap<>(properties));

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
			ResourcePassthroughException.maybeRethrow(t);
			fireResourceCreationEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else {
				throw new ResourceCreationException(RESOURCE_BUNDLE_DEFAULT,
						"generic.create.error", t.getMessage(),t );
			}
		}


	}

	protected void beforeCreateResource(T resource, Map<String,String> properties) throws ResourceException {

	}

	protected void afterCreateResource(T resource, Map<String,String> properties) throws ResourceException {

	}

	protected void beforeUpdateResource(T resource, Map<String,String> properties) throws ResourceException {

	}

	protected void afterUpdateResource(T resource, Map<String,String> properties) throws ResourceException {

	}

	protected void beforeDeleteResource(T resource) throws ResourceException, AccessDeniedException {

	}

	protected void afterDeleteResource(T resource) throws ResourceException, AccessDeniedException {

	}

	protected abstract void fireResourceCreationEvent(T resource);

	protected abstract void fireResourceCreationEvent(T resource, Throwable t);

	protected void processName(T resource, Map<String,String> properties) {
		if(properties!=null && properties.containsKey("name")) {
			resource.setName(properties.get("name"));
		}
	}
	
	@Override
	@SafeVarargs
	public final void updateResource(T resource, 
			Map<String,String> properties, 
			TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {

		processName(resource, properties);
		
		/**
		 * LDP = If we pass a null set of properties then assume the resource has changed. We need this because several
		 * areas don't use properties and this method prevents the essential update events from being fired because it
		 * assumes the changes are in the properties map.
		 */
		boolean changedDefault = properties==null || (resource.getOldName()!=null && !resource.getName().equals(resource.getOldName()))     ;

		if(assertPermissions) {
			assertPermission(getUpdatePermission(resource));
		}

		if(resource.getRealm()==null) {
			resource.setRealm(isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
			changedDefault = true;
		}

		if(!checkUnique(resource, false)) {
			ResourceChangeException ex = new ResourceChangeException(
					RESOURCE_BUNDLE_DEFAULT, "generic.alreadyExists.error",
				resource.getName());
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

			if(changedDefault) {
				fireResourceUpdateEvent(resource);
			}
		} catch (Throwable t) {
			if(changedDefault) {
				fireResourceUpdateEvent(resource, t);
			}
			if (t instanceof ResourceChangeException) {
				throw (ResourceChangeException) t;
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE_DEFAULT,
						"generic.update.error", t.getMessage(), t);
			}
		}
	}


	protected List<PropertyChange> calculateChanges(T resource, Map<String,String> properties) {
		return getRepository().calculateChanges(resource, properties);
	}

	protected void populateEntityFields(T resource, Map<String,String> properties) {
		getRepository().populateEntityFields(resource, properties);
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
	@SafeVarargs
	public final void updateResource(T resource, TransactionOperation<T>... ops) throws ResourceException,
			AccessDeniedException {
		updateResource(resource, null, ops);
	}

	protected abstract void fireResourceUpdateEvent(T resource);

	protected abstract void fireResourceUpdateEvent(T resource, Throwable t);

	@SafeVarargs
	@Override
	public final void deleteResource(T resource, TransactionOperation<T>... ops) throws ResourceException,
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
			if(t instanceof DataIntegrityViolationException) {
				throw new ResourceChangeException(RESOURCE_BUNDLE_DEFAULT, "error.objectInUse");
			}
			fireResourceDeletionEvent(resource, t);
			if (t instanceof ResourceChangeException) {
				throw (ResourceChangeException) t;
			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE_DEFAULT,
						"generic.delete.error", t.getMessage(), t);
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
	public Collection<T> allRealmsResources() {
		return getRepository().allRealmsResources();
	}
	
	@Override
	public T getResourceByName(String name) throws ResourceNotFoundException {
		return getResourceByName(name, false);
	}

	@Override
	public T getResourceByName(String name, boolean searchAllRealms) throws ResourceNotFoundException {
		T resource = getRepository().getResourceByName(name, searchAllRealms ? null : isSystemResource() ? realmService.getSystemRealm() : getCurrentRealm());
		if (resource == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE_DEFAULT,
					"error.invalidResourceName", name);
		}
		return resource;
	}

	@Override
	public T getResourceByName(String name, Realm realm) throws ResourceNotFoundException {

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
	public T getResourceByLegacyId(Long id) throws ResourceNotFoundException, AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		T resource = getRepository().getResourceByLegacyId(id);
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
	public String getResourceDecryptedValue(T resource, String resourceKey) {
		return getRepository().getDecryptedValue(resource, resourceKey);
	}

	@Override
	public String exportResources(@SuppressWarnings("unchecked") T... resources) throws ResourceExportException, AccessDeniedException {
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
	public String exportAllResoures() throws ResourceExportException, AccessDeniedException {
		List<T> list = allResources();
		return exportResources(list);
	}

	protected boolean isExportingAdditionalProperties() {
		return true;
	}

	protected void prepareExport(T resource) throws ResourceException, AccessDeniedException {
		if(isExportingAdditionalProperties()) {
			resource.setProperties(getRepository().getProperties(resource));
		}
	}

	protected void prepareImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Preparing import %s", resource.getName()));
		}
		
		resource.preserveTimestamp();
	}

	@Override
	public String exportResources(Collection<T> resources) throws ResourceExportException, AccessDeniedException {

		if(resources.isEmpty()) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.nothingToExport");
		}

		ObjectMapper mapper = new ObjectMapper();


		try {
			for(T resource : resources) {
				prepareExport(resource);
				resource.setLegacyId(resource.getId());
				resource.setId(null);
				resource.setRealm(null);
			}

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources);
		} catch (JsonProcessingException e) {
			throw new ResourceExportException(RESOURCE_BUNDLE_DEFAULT, "error.exportError", e.getMessage());
		} catch (ResourceException e) {
			throw new ResourceExportException(e);
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
						onCompleteImportDropResources(realm);
					}
					
					
					ObjectMapper mapper = new ObjectMapper();

					Collection<T> resources = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, getResourceClass()));
					for(T resource : resources) {
						performImport(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
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

	protected void onCompleteImportDropResources(Realm realm) throws AccessDeniedException, ResourceException {
		
	}
	
	@Override
	public final String getFingerprint() {
		return fingerprint;
	}

	protected void performImportDropResources(T resource) throws ResourceException, AccessDeniedException {
		deleteResource(resource);
	}

	protected void performImport(T resource, Realm realm) throws ResourceException, AccessDeniedException {
		resource.setRealm(realm);
		checkImportName(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
		createResource(resource, resource.getProperties()==null ? new HashMap<String,String>() :
			ResourceUtils.filterResourceProperties(getRepository().getPropertyTemplates(null), resource.getProperties()));
	}

	@Override
	public void checkImportName(T resource, Realm realm) throws ResourceException, AccessDeniedException {

		prepareImport(resource, isSystemResource() ? realmService.getSystemRealm() : realm);
		
		String name = resource.getName();
		int i=1;
		while(!checkUnique(resource, true)) {
			resource.setName(String.format("%s [#%d]", name, i++));
		}
	}

	protected final void updateFingerprint() {
		fingerprint = new BigInteger(130, random).toString(32);
	}

	protected void prepareCopy(T resource) throws ResourceException {

	}

	@Override
	public T copyResource(T resource) throws ResourceException, AccessDeniedException {

		resource.setId(null);
		String name = resource.getName();
		do {
			name = name + " [copy]";
		} while(getRepository().getResourceByName(name, getCurrentRealm())!=null);

		resource.setName(name);

		prepareCopy(resource);

		createResource(resource);
		return resource;
	}

	@Override
	public void deleteResources(final List<T> resources,
			@SuppressWarnings("unchecked") final TransactionOperation<T>... ops)
			throws ResourceException, AccessDeniedException {

		if(assertPermissions) {
			assertPermission(getDeletePermission());
		}

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
		if(assertPermissions) {
			assertPermission(getReadPermission());
		}

		return getRepository().getResourcesByIds(ids);
	}

}
