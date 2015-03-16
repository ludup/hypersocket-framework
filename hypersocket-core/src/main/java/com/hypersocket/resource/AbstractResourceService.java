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

	void createResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceCreationException, AccessDeniedException;

	void updateResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceChangeException, AccessDeniedException;

	void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> searchResources(Realm realm, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	long getResourceCount(Realm realm, String search)
			throws AccessDeniedException;

	List<T> getResources();

	T getResourceByName(String name) throws ResourceNotFoundException;

	T getResourceById(Long id) throws ResourceNotFoundException;

	Collection<PropertyCategory> getResourceTemplate();

	Collection<PropertyCategory> getResourceProperties(T resource);

	String getResourceProperty(T resource, String resourceKey);

	boolean getResourceBooleanProperty(T resource, String resourceKey);

	int getResourceIntProperty(T resource, String resourceKey);

	T getResourceByName(String name, Realm realm)
			throws ResourceNotFoundException;

}
