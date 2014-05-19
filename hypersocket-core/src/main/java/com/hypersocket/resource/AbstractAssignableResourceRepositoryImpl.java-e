/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;

@Repository
@Transactional
public abstract class AbstractAssignableResourceRepositoryImpl<T extends AssignableResource>
		extends AbstractRepositoryImpl<Long> implements
		AbstractAssignableResourceRepository<T> {

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getAssignableResources(
			List<Principal> principals) {

		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (List<T>) crit.list();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<AssignableResource> getAllAssignableResources(
			List<Principal> principals) {
		
		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(AssignableResource.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (List<AssignableResource>) crit.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getResourceByIdAndPrincipals(Long resourceId, List<Principal> principals) {
		
		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(getResourceClass());
		crit.add(Restrictions.eq("id", resourceId));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (T)crit.uniqueResult();
	}

	protected <K extends AssignableResourceSession<T>> K createResourceSession(
			T resource, Session session, K newSession) {
		
		newSession.setSession(session);
		newSession.setResource(resource);
		
		save(newSession);
		
		return newSession;
	}
	
	@Override
	public T getResourceByName(String name) {
		return get("name", name, getResourceClass(), new DeletedCriteria(false));
	}
	
	@Override
	public T getResourceByName(String name, boolean deleted) {
		return get("name", name, getResourceClass(), new DeletedCriteria(deleted));
	}
	
	@Override
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	public void deleteResource(T resource)
			throws ResourceChangeException {
		
		delete(resource);
	}
	
	@Override
	public void saveResource(T resource) {
		save(resource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResources(Realm realm) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		if(realm!=null) {
			crit.add(Restrictions.eq("realm", realm));
		}
		return (List<T>)crit.list();
	}
	
	@Override
	public List<T> getResources() {
		return getResources(null);
	}
	
	@Override
	public List<T> search(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return super.search(getResourceClass(), "name", searchPattern, start, length, sorting, configs);
	}
	
	@Override
	public long getResourceCount(Realm realm, String searchPattern, CriteriaConfiguration... configs) {
		return getCount(getResourceClass(), "name", searchPattern, configs);
	}
	
	protected abstract Class<T> getResourceClass();
	
}
