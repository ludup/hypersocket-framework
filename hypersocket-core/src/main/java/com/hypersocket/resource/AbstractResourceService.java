package com.hypersocket.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface AbstractResourceService<T extends RealmResource> extends
		AuthenticatedService {

	@SuppressWarnings("unchecked") 
	void createResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceCreationException, AccessDeniedException;

	@SuppressWarnings("unchecked") 
	void createResource(T resource, TransactionOperation<T>... ops)
			throws ResourceCreationException, AccessDeniedException;
	
	@SuppressWarnings("unchecked") 
	void updateResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceChangeException, AccessDeniedException;
	
	@SuppressWarnings("unchecked") 
	void updateResource(T resource, TransactionOperation<T>... ops)
			throws ResourceChangeException, AccessDeniedException;

	void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> searchResources(Realm realm, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	long getResourceCount(Realm realm, String search)
			throws AccessDeniedException;

	List<T> allResources();

	T getResourceByName(String name) throws ResourceNotFoundException;

	T getResourceById(Long id) throws ResourceNotFoundException;

	Collection<PropertyCategory> getResourceTemplate();

	Collection<PropertyCategory> getResourceProperties(T resource);

	String getResourceProperty(T resource, String resourceKey);

	boolean getResourceBooleanProperty(T resource, String resourceKey);

	int getResourceIntProperty(T resource, String resourceKey);

	T getResourceByName(String name, Realm realm)
			throws ResourceNotFoundException;

	String exportResources(Collection<T> resources)
			throws ResourceExportException;

	String exportResources(@SuppressWarnings("unchecked") T... resources) throws ResourceExportException;

	Collection<T> importResources(String json, Realm realm)
			throws AccessDeniedException, ResourceException;

	String getResourceCategory();

	String exportResoure(Long id) throws ResourceNotFoundException,
			ResourceExportException;

	String exportAllResoures() throws ResourceExportException;
	
	void extendPropertyTemplates(String string);

}
