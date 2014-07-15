package com.hypersocket.resource;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepository;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface AbstractResourceRepository<T extends RealmResource> extends AbstractRepository<Long> {

	T getResourceByName(String name);

	T getResourceByName(String name, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource) throws ResourceChangeException;

	void saveResource(T resource);

	List<T> getResources(Realm realm);

	List<T> getResources();

	List<T> search(Realm realm, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchPattern,
			CriteriaConfiguration... configs);

}
