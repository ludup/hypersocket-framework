/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.List;
import java.util.Map;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface AbstractAssignableResourceRepository<T extends AssignableResource>
		extends AssignableResourceRepository {

	List<T> getAssigedResources(List<Principal> principals);
	
	T getResourceByIdAndPrincipals(Long resourceId, List<Principal> principals);

	T getResourceByName(String name);

	T getResourceByName(String name, boolean deleted);

	T getResourceById(Long id);

	void deleteResource(T resource) throws ResourceChangeException;

	List<T> getResources(Realm realm);

	void saveResource(T resource, Map<String,String> properties);

	List<T> search(Realm realm, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchPattern,
			CriteriaConfiguration... configs);

	List<T> getAssignedResources(Principal... principals);

	Long getAssignableResourceCount(Principal... principals);

	Long getAssignedResourceCount(Principal principal, 
			String searchPattern,
			CriteriaConfiguration... configs);

	List<T> searchAssignedResources(Principal principal, 
			String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);
	
}
