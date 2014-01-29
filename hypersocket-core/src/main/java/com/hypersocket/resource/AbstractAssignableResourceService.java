package com.hypersocket.resource;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public interface AbstractAssignableResourceService<T> extends AuthenticatedService {

	void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> getResources(Realm realm) throws AccessDeniedException;

	T getResourceByName(String name) throws ResourceNotFoundException;
	
	T getResourceById(Long id) throws ResourceNotFoundException;

	List<T> getResources(Principal principal) throws AccessDeniedException;

	void createResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	void updateResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> getResources();

}
