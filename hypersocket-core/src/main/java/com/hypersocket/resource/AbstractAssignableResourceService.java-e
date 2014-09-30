package com.hypersocket.resource;

import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface AbstractAssignableResourceService<T> extends AuthenticatedService {

	void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> getResources() throws AccessDeniedException;
	
	T getResourceByName(String name) throws ResourceNotFoundException;
	
	T getResourceById(Long id) throws ResourceNotFoundException;

	List<T> getResources(Principal principal) throws AccessDeniedException;

	void createResource(T resource, Map<String,String> properties) throws 
			AccessDeniedException, ResourceCreationException;

	void updateResource(T resource, Map<String,String> properties) throws ResourceChangeException,
			AccessDeniedException;

	List<T> searchResources(Realm realm, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	long getResourceCount(Realm realm, String search)
			throws AccessDeniedException;

	List<T> searchPersonalResources(Principal principal, String search,
			int start, int length, ColumnSort[] sorting);

	long getPersonalResourceCount(Principal principal, String search);

	List<T> getPersonalResources(Principal principal);

}
