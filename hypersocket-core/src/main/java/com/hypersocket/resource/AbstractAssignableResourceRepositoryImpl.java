/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.session.Session;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Repository
public abstract class AbstractAssignableResourceRepositoryImpl<T extends AssignableResource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractAssignableResourceRepository<T> {


	protected EntityResourcePropertyStore entityPropertyStore;

	@Autowired
	EncryptionService encryptionService;
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(encryptionService);
	}

	@Override
	protected ResourcePropertyStore getPropertyStore() {
		return entityPropertyStore;
	}
	
	@Override 
	public EntityResourcePropertyStore getEntityStore() {
		return entityPropertyStore;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<T> getAssignedResources(Role role, CriteriaConfiguration... configs) {

		
		Criteria criteria = createCriteria(getResourceClass());
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		criteria.add(Restrictions.eq("realm", role.getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("id", role.getId()));
		
		return criteria.list();

	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<T> getAssignedResources(List<Principal> principals, CriteriaConfiguration... configs) {

		
		Criteria criteria = createCriteria(getResourceClass());
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<T> everyone = new HashSet<T>(criteria.list());
		
		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}

		criteria = createCriteria(getResourceClass());
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		criteria.add(Restrictions.in("id", ids));
		
		everyone.addAll((List<T>) criteria.list());
		return everyone;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<T> searchAssignedResources(List<Principal> principals,
			final String searchPattern, final int start, final int length,
			final ColumnSort[] sorting, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());

		
		criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);

		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> principalIds = new ArrayList<Long>();
		for(Principal p : principals) {
			principalIds.add(p.getId());
		}
		criteria.add(Restrictions.in("id", principalIds));
		
		ids.addAll(criteria.list());
		
		if(ids.size() > 0) {
			
			criteria = createCriteria(getResourceClass());
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.in("id", ids));
	
			criteria.setFirstResult(start);
			criteria.setMaxResults(length);
			
			for (ColumnSort sort : sorting) {
				criteria.addOrder(sort.getSort() == Sort.ASC ? Order.asc(sort
						.getColumn().getColumnName()) : Order.desc(sort.getColumn()
						.getColumnName()));
			}
			
			return ((List<T>) criteria.list());
		}
		
		return new ArrayList<T>();
	};
	

	@Override
	@Transactional(readOnly=true)
	public boolean hasAssignedEveryoneRole(Realm realm, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		criteria.setProjection(Projections.property("id"));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", realm));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<?> everyoneRoles = criteria.list();
		
		return everyoneRoles.size() > 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<Principal> getAssignedPrincipals(Realm realm, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", realm));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		
		criteria = criteria.createCriteria("principals");
		criteria.setProjection(Projections.distinct(Projections.property("id")));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		List<?> uniquePrincipals = criteria.list();
		
		if(uniquePrincipals.isEmpty()) {
			return new HashSet<Principal>();
		}
		criteria = createCriteria(Principal.class);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.in("id", uniquePrincipals));
		List<?> res = criteria.list();
		
		return (Collection<Principal>) res;
	}
		
	@Override
	@Transactional(readOnly=true)
	public Long getAssignedPrincipalCount(Realm realm, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", realm));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		
		criteria = criteria.createCriteria("principals");
		criteria.setProjection(Projections.distinct(Projections.property("id")));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		List<?> uniquePrincipals = criteria.list();
		
		return (long)uniquePrincipals.size();

	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Long getAssignedResourceCount(List<Principal> principals,
			final String searchPattern, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());

		
		criteria = createCriteria(getResourceClass());
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> principalIds = new ArrayList<Long>();
		for(Principal p : principals) {
			principalIds.add(p.getId());
		}
		criteria.add(Restrictions.in("id", principalIds));
		
		ids.addAll((List<Long>)criteria.list());
		
		return new Long(ids.size());
	}

	@Override
	@Transactional(readOnly=true)
	public Long getAssignableResourceCount(List<Principal> principals) {
		return getAssignedResourceCount(principals, "");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<AssignableResource> getAllAssignableResources(
			List<Principal> principals) {

		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = createCriteria(AssignableResource.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (List<AssignableResource>) crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public T getResourceByIdAndPrincipals(Long resourceId,
			List<Principal> principals) {

		Set<Long> ids = new HashSet<Long>();
		for (Principal p : principals) {
			ids.add(p.getId());
		}
		Criteria crit = createCriteria(getResourceClass());
		crit.add(Restrictions.eq("id", resourceId));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit = crit.createCriteria("roles");
		crit = crit.createCriteria("principals");
		crit.add(Restrictions.in("id", ids));

		return (T) crit.uniqueResult();
	}

	protected <K extends AssignableResourceSession<T>> K createResourceSession(
			T resource, Session session, K newSession) {

		newSession.setSession(session);
		newSession.setResource(resource);

		save(newSession);

		return newSession;
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm) {
		return get("name", name, getResourceClass(), new DeletedCriteria(false), new RealmRestriction(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm, boolean deleted) {
		return get("name", name, getResourceClass(), new DeletedCriteria(
				deleted), new RealmRestriction(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	@Transactional
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceChangeException {
		beforeDelete(resource);
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, null);
		}
		delete(resource);
		afterDelete(resource);
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, null);
		}
	}

	protected void beforeDelete(T resource) throws ResourceChangeException {
		
	}
	
	protected void afterDelete(T resource) throws ResourceChangeException {
		
	}
	
	protected void beforeSave(T resource, Map<String,String> properties) throws ResourceChangeException {
		
	}
	
	protected void afterSave(T resource, Map<String,String> properties) throws ResourceChangeException {
		
	}
	
	@Override
	public void populateEntityFields(T resource, Map<String,String> properties) {
		
		for(String resourceKey : getPropertyNames(resource)) {
			if(properties.containsKey(resourceKey)) {
				PropertyTemplate template = getPropertyTemplate(resource, resourceKey);
				if(template.getPropertyStore() instanceof EntityResourcePropertyStore) {
					setValue(resource, resourceKey, properties.get(resourceKey));
					properties.remove(resourceKey);
				}
			}
		}
	}
	
	@Override
	@SafeVarargs
	@Transactional
	public final void saveResource(T resource, Map<String, String> properties, TransactionOperation<T>... ops) throws ResourceChangeException {

		
		beforeSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, properties);
		}

		save(resource);

		// Now set any remaining values
		setValues(resource, properties);
		
		afterSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, properties);
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<T> getResources(Realm realm) {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.eq("realm", realm));

		return (List<T>) crit.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<T> allResources() {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.setFetchMode("roles", FetchMode.SELECT);
		crit.add(Restrictions.eq("deleted", false));

		return (List<T>) crit.list();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> search(Realm realm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		List<T> results = super.search(getResourceClass(), searchColumn, searchPattern, start,
				length, sorting, ArrayUtils.addAll(configs, new DeletedCriteria(false),
						new RoleSelectMode(), new RealmCriteria(
								realm)));
		return results;
	}
	
	
	@Override
	@Transactional(readOnly=true)
	public long allRealmsResourcesCount() {
		return getCount(getResourceClass(), new DeletedCriteria(false));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		long count = getCount(getResourceClass(), searchColumn, searchPattern,
				ArrayUtils.addAll(configs, new RoleSelectMode(), new DeletedCriteria(false),
						new RealmCriteria(realm)));
		return count;
	}

	protected abstract Class<T> getResourceClass();

	class RoleSelectMode implements CriteriaConfiguration {

		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("roles", FetchMode.SELECT);
		}
	}
	

	@SuppressWarnings("unchecked")
	protected Collection<T> searchResources(Realm currentRealm, 
			String name, 
			Date createdFrom, Date createdTo, 
			Date lastUpdatedFrom, Date lastUpdatedTo, 
			CriteriaConfiguration... configs) {
	
		Criteria criteria = createCriteria(getResourceClass());
		
		for (String property : resolveCollectionProperties(getResourceClass())) {
			 criteria.setFetchMode(property, org.hibernate.FetchMode.SELECT);
		}
		
		if (!StringUtils.isEmpty(name)) {
			criteria.add(Restrictions.ilike("name", name.replace('*', '%')));
		}
		
		criteria.add(Restrictions.eq("realm", currentRealm));
		criteria.add(Restrictions.eq("deleted", false));
		
		if(createdFrom!=null) {
			criteria.add(Restrictions.and(Restrictions.ge("created", createdFrom),
					Restrictions.lt("created", createdTo)));
		}
		
		if(lastUpdatedFrom!=null) {
			criteria.add(Restrictions.and(Restrictions.ge("modified", lastUpdatedFrom),
					Restrictions.lt("modified", lastUpdatedTo)));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		return (Collection<T>) criteria.list();
	}

}
