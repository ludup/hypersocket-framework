package com.hypersocket.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.auth.PasswordEnabledAuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface AbstractAssignableResourceService<T> extends PasswordEnabledAuthenticatedService {
	
	String getFingerprint();

	List<T> allResources();
	
	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> getResources();
	
	T getResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException;
	
	T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;

	Collection<T> getResources(Principal principal) throws AccessDeniedException;

	@SuppressWarnings("unchecked") 
	void createResource(T resource, Map<String,String> properties,
			TransactionOperation<T>... ops) throws 
			AccessDeniedException, ResourceCreationException;

	@SuppressWarnings("unchecked") 
	void updateResource(T resource, Map<String,String> properties,
			TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException;

	List<T> searchResources(Realm realm, String searchColumn, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	long getResourceCount(Realm realm, String searchColumn, String search)
			throws AccessDeniedException;

	Collection<T> searchPersonalResources(Principal principal, String searchColumn, String search,
			int start, int length, ColumnSort[] sorting);

	long getPersonalResourceCount(Principal principal, String searchColumn, String search);

	Collection<T> getPersonalResources(Principal principal);
	
	Collection<T> getPersonalResources();

	long getPersonalResourceCount(String search);

	void updateResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops)
			throws ResourceChangeException, AccessDeniedException;

	String exportResoure(Long id) throws ResourceNotFoundException,
			ResourceExportException, AccessDeniedException;

	String exportAllResoures() throws ResourceExportException;

	T getResourceByName(String name, Realm realm)
			throws ResourceNotFoundException, AccessDeniedException;

	String getResourceCategory();

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops)
			throws ResourceChangeException, AccessDeniedException;

	long getPrincipalsInUse(Realm realm) throws AccessDeniedException;

	Collection<T> importResources(String json, Realm realm, boolean dropCurrent)
			throws AccessDeniedException, ResourceException;

	void saveMetaData(T resource, String key, String value) throws AccessDeniedException;
	
	String getMetaData(T resource, String key, String defaultValue) throws AccessDeniedException;

	long getPersonalResourceCount();

	Collection<T> getPersonalResources(Principal principal, boolean resolveAssosicatedPrincipals);

	Collection<T> getPersonalRoleResources(Principal principal);

	String exportResources(Collection<T> resources, boolean stripIdentity) throws ResourceExportException;

	String exportResources(boolean stripIdentity, @SuppressWarnings("unchecked") T... resources) throws ResourceExportException;

	Collection<PropertyCategory> getResourceTemplate();

	Collection<PropertyCategory> getResourceProperties(T resource) throws AccessDeniedException;

	String getResourceProperty(T resource, String resourceKey);

	boolean getResourceBooleanProperty(T resource, String resourceKey);

	Long getResourceLongProperty(T resource, String resourceKey);

	int getResourceIntProperty(T resource, String resourceKey);
}
