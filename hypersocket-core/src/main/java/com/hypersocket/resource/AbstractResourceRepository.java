package com.hypersocket.resource;

import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

import java.util.List;
import java.util.Map;

public interface AbstractResourceRepository<T extends Resource> extends ResourceTemplateRepository, FindableResourceRepository<T> {

	T getResourceByName(String name, Realm realm);

	T getResourceByName(String name, Realm realm, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException;
      
	@SuppressWarnings("unchecked") 
	void saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) throws ResourceException;

	void saveResource(T resource);
	
	List<T> getResources(Realm realm);

	List<T> search(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs);

	long allRealmsResourcesCount();

	List<PropertyChange> populateEntityFields(T resource, Map<String, String> properties);

	EntityResourcePropertyStore getEntityStore();

}
