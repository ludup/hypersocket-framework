package com.hypersocket.resource;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface AbstractResourceService<T extends RealmResource> extends AuthenticatedService {

	void createResource(T resource) throws ResourceCreationException,
			AccessDeniedException;

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

}
