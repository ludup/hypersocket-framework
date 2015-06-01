package com.hypersocket.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.auth.PasswordEnabledAuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface AbstractAssignableResourceService<T> extends PasswordEnabledAuthenticatedService {

	void deleteResource(T resource) throws ResourceChangeException,
			AccessDeniedException;

	List<T> allResources();
	
	List<T> getResources(Realm realm) throws AccessDeniedException;

	List<T> getResources();
	
	T getResourceByName(String name) throws ResourceNotFoundException;
	
	T getResourceById(Long id) throws ResourceNotFoundException;

	Collection<T> getResources(Principal principal) throws AccessDeniedException;

	@SuppressWarnings("unchecked") 
	void createResource(T resource, Map<String,String> properties,
			TransactionOperation<T>... ops) throws 
			AccessDeniedException, ResourceCreationException;

	@SuppressWarnings("unchecked") 
	void updateResource(T resource, Map<String,String> properties,
			TransactionOperation<T>... ops) throws ResourceChangeException,
			AccessDeniedException;

	List<T> searchResources(Realm realm, String search, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	long getResourceCount(Realm realm, String search)
			throws AccessDeniedException;

	Collection<T> searchPersonalResources(Principal principal, String search,
			int start, int length, ColumnSort[] sorting);

	long getPersonalResourceCount(Principal principal, String search);

	Collection<T> getPersonalResources(Principal principal) throws AccessDeniedException;
	
	Collection<T> getPersonalResources() throws AccessDeniedException;

	long getPersonalResourceCount(String search);

}
