/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import com.hypersocket.bulk.BulkAssignment;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AbstractAssignableResourceRepository<T extends AssignableResource>
		extends AssignableResourceRepository, FindableResourceRepository<T> {

	T getResourceByIdAndPrincipals(Long resourceId, List<Principal> principals);

	T getResourceByName(String name, Realm realm);

	T getResourceByName(String name, Realm realm, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops)
			throws ResourceException;

	List<T> getResources(Realm realm);

	@SuppressWarnings("unchecked")
	List<PropertyChange> saveResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops)
			throws ResourceException;

	List<T> search(Realm realm, String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchColumn, String searchPattern, CriteriaConfiguration... configs);
	
	Long getAssignableResourceCount(Collection<Principal> principal);

	Long getAssignedResourceCount(Collection<Principal> principals, String searchPattern, String searchColumn, 
			CriteriaConfiguration... configs);

	Collection<T> searchAssignedResources(List<Principal> principals, String searchPattern, String searchColumn, 
			int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	List<T> allResources();

	long allRealmsResourcesCount();

	List<PropertyChange> populateEntityFields(T resource, Map<String, String> properties);

	boolean hasAssignedEveryoneRole(Realm realm, CriteriaConfiguration... configs);

	EntityResourcePropertyStore getEntityStore();

	Collection<T> getAssignedResources(List<Principal> principals, CriteriaConfiguration... configs);

	Collection<T> getAssignedResources(String name, List<Principal> principals, CriteriaConfiguration... configs);

	Collection<T> getAssignedResources(Role role, CriteriaConfiguration... configs);

	long getResourceByRoleCount(Realm realm, Role... roles);

	Collection<T> getResourcesByRole(Realm currentRealm, Role... role);

	T getPersonalResourceByName(String name, Principal principal, CriteriaConfiguration... configs);

	void bulkAssignRolesToResource(BulkAssignment bulkAssignment);

	void removeAssignments(Role role);

	void deleteResources(List<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException;
	
	boolean isDeletable();
	
	List<T> allRealmResources(Realm realm);
}
