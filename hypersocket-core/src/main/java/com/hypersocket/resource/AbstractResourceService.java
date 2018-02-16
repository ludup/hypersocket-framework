package com.hypersocket.resource;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AbstractResourceService<T extends RealmResource> extends AuthenticatedService {

	@SuppressWarnings("unchecked") 
	void createResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws AccessDeniedException, ResourceException;

	@SuppressWarnings("unchecked") 
	void createResource(T resource, TransactionOperation<T>... ops)
			throws ResourceException, AccessDeniedException;
	
	@SuppressWarnings("unchecked") 
	void updateResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceException, AccessDeniedException;

	@SuppressWarnings("unchecked") 
	void updateResource(T resource, TransactionOperation<T>... ops)
			throws ResourceException, AccessDeniedException;

	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> searchResources(Realm realm, String searchColumn, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;


	long getResourceCount(Realm realm, String searchColumn, String search) throws AccessDeniedException;

	List<T> allResources();

	T getResourceByName(String name) throws ResourceNotFoundException, AccessDeniedException;

	T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;

	T getResourceByLegacyId(Long id) throws ResourceNotFoundException, AccessDeniedException;
	
	Collection<PropertyCategory> getResourceTemplate();

	Collection<PropertyCategory> getResourceProperties(T resource);

	String getResourceProperty(T resource, String resourceKey);

	boolean getResourceBooleanProperty(T resource, String resourceKey);

	int getResourceIntProperty(T resource, String resourceKey);

	Long getResourceLongProperty(T resource, String resourceKey);
	
	T getResourceByName(String name, Realm realm)
			throws ResourceNotFoundException, AccessDeniedException;

	String exportResources(Collection<T> resources)
			throws ResourceExportException, AccessDeniedException, ResourceNotFoundException;

	String exportResources(@SuppressWarnings("unchecked") T... resources) throws ResourceExportException, AccessDeniedException, ResourceNotFoundException;

	String getResourceCategory();

	String exportResoure(Long id) throws ResourceNotFoundException,
			ResourceExportException, AccessDeniedException;

	String exportAllResoures() throws ResourceExportException, AccessDeniedException, ResourceNotFoundException;
	
	void extendPropertyTemplates(String string);

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops)
			throws ResourceException, AccessDeniedException;

	Collection<T> importResources(String json, Realm realm, boolean dropCurrent)
			throws AccessDeniedException, ResourceException;

	String getFingerprint();

	T getResourceByName(String name, 
			boolean searchAllRealms) throws ResourceNotFoundException;

	T copyResource(T resource) throws ResourceException, AccessDeniedException;

	void deleteResources(List<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) 
			throws ResourceException, AccessDeniedException;
	
	List<T> getResourcesByIds(Long...ids) throws AccessDeniedException;

	Collection<T> allRealmsResources();

}
