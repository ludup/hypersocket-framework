/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface AbstractAssignableResourceRepository<T extends AssignableResource>
		extends AssignableResourceRepository {

	T getResourceByIdAndPrincipals(Long resourceId, List<Principal> principals);

	T getResourceByName(String name, Realm realm);

	T getResourceByName(String name, Realm realm, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops)
			throws ResourceChangeException;

	List<T> getResources(Realm realm);

	@SuppressWarnings("unchecked")
	void saveResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceChangeException;

	List<T> search(Realm realm, String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchColumn, String searchPattern, CriteriaConfiguration... configs);
	
	Long getAssignableResourceCount(List<Principal> principal);

	Long getAssignedResourceCount(List<Principal> principals, String searchPattern, CriteriaConfiguration... configs);

	Collection<T> searchAssignedResources(List<Principal> principals, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	List<T> allResources();

	long allRealmsResourcesCount();

	void populateEntityFields(T resource, Map<String, String> properties);

	boolean hasAssignedEveryoneRole(Realm realm, CriteriaConfiguration... configs);

	Long getAssignedPrincipalCount(Realm realm, CriteriaConfiguration... configs);

	Collection<Principal> getAssignedPrincipals(Realm realm, CriteriaConfiguration... configs);

	EntityResourcePropertyStore getEntityStore();

	Collection<T> getAssignedResources(List<Principal> principals, CriteriaConfiguration... configs);

	Collection<T> getAssignedResources(Role role, CriteriaConfiguration... configs);



}
