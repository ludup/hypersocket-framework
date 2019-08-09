/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.resource.SimpleResource;
import com.hypersocket.resource.ResourceRestriction;

@Repository
public abstract class PropertyRepositoryImpl extends AbstractRepositoryImpl<Long> implements PropertyRepository {

	
	public PropertyRepositoryImpl(boolean requiresDemoWrite) {
		super(requiresDemoWrite);
	}
	
	public PropertyRepositoryImpl() {
		super(false);
	}

	@Override
	@Transactional
	public void saveProperty(DatabaseProperty item) {
		save(item);
	}
	
	@Override
	@Transactional(readOnly=true)
	public DatabaseProperty getProperty(String resourceKey) {
		return get("resourceKey", resourceKey, DatabaseProperty.class, new DistinctRootEntity(), new NullValueRestriction("resource"));
	}

	@Override
	@Transactional(readOnly=true)
	public Property getProperty(Long id) {
		return get("id", id, DatabaseProperty.class);
	}

	@Override
	@Transactional(readOnly=true)
	public DatabaseProperty getProperty(String resourceKey, SimpleResource resource) {
		return get("resourceKey", resourceKey, DatabaseProperty.class, new ResourceRestriction(resource));
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<DatabaseProperty> getPropertiesWithValue(String resourceKey, String value) {
		return list("resourceKey", resourceKey, DatabaseProperty.class, new ValueRestriction(value));
	}

	@Override
	@Transactional(readOnly=true)
	public List<DatabaseProperty> getPropertiesForResource(SimpleResource resource) {
		return list("resource", resource.getId(), DatabaseProperty.class);
	}

	@Override
	@Transactional
	public void deletePropertiesForResource(SimpleResource resource) {
		deleteProperties(resource);
	}
	
	@Override
	@Transactional
	public void deleteProperties(SimpleResource resource, String... resourceKeys) {
		
		Query query = null;
		if(resourceKeys.length > 0) {
			query = createQuery("delete from DatabaseProperty where resourceKey in (:resourceKeys) and resource = :resource", true);
			query.setParameterList("resourceKeys", resourceKeys);
			query.setParameter("resource", resource.getId());
		} else {
			query = createQuery("delete from DatabaseProperty where resource = :resource", true);
			query.setParameter("resource", resource.getId());
		}
		query.executeUpdate();
	}
	
}
