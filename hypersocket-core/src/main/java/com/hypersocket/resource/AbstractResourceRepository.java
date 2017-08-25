package com.hypersocket.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface AbstractResourceRepository<T extends AbstractResource> extends ResourceTemplateRepository, FindableResourceRepository<T> {

	T getResourceByName(String name, Realm realm);

	T getResourceByName(String name, Realm realm, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException;
      
	@SuppressWarnings("unchecked") 
	List<PropertyChange> saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceException;

	List<PropertyChange> saveResource(T resource) throws ResourceException;
	
	List<T> getResources(Realm realm);

	List<T> search(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs);

	long allRealmsResourcesCount();

	void populateEntityFields(T resource, Map<String, String> properties);

	List<PropertyChange> calculateChanges(T resource, Map<String, String> properties);

	EntityResourcePropertyStore getEntityStore();

	long getResourceCount(Realm realm);

	void deleteResources(List<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException;

	long getResourceCount(Collection<Realm> realms, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs);


}
