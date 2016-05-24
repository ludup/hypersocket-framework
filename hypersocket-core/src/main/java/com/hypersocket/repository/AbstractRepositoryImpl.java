/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Repository
public abstract class AbstractRepositoryImpl<K> implements AbstractRepository<K> {

	static Logger log = LoggerFactory.getLogger(AbstractRepositoryImpl.class);

	private HibernateTemplate hibernateTemplate;
	private SessionFactory sessionFactory;

	private boolean requiresDemoWrite = false;

	protected AbstractRepositoryImpl() {

	}

	protected AbstractRepositoryImpl(boolean requiresDemoWrite) {
		this.requiresDemoWrite = requiresDemoWrite;
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		hibernateTemplate = new HibernateTemplate(this.sessionFactory = sessionFactory);
		hibernateTemplate.setCacheQueries(true);
	}

	private void checkDemoMode() {
		if (Boolean.getBoolean("hypersocket.demo") && !requiresDemoWrite) {
			throw new IllegalStateException("This is a demo. No changes to resources or settings can be persisted.");
		}
	}

	@Transactional
	protected void save(AbstractEntity<K> entity) {

		checkDemoMode();

		entity.setLastModified(new Date());

		if (entity.getId() != null) {
			hibernateTemplate.merge(entity);
		} else {
			hibernateTemplate.saveOrUpdate(entity);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	protected void save(Object entity, boolean isNew) {

		checkDemoMode();

		if(entity instanceof AbstractEntity<?>) {
			((AbstractEntity<?>)entity).setLastModified(new Date());
		}
		if (!isNew) {
			hibernateTemplate.merge(entity);
		} else {
			hibernateTemplate.saveOrUpdate(entity);
		}
	}

	protected <T> T load(Class<T> entityClass, Long id) {
		return hibernateTemplate.load(entityClass, id);
	}

	protected Query createQuery(String hql, boolean isWritable) {

		if (isWritable) {
			checkDemoMode();
		}
		return sessionFactory.getCurrentSession().createQuery(hql);
	}

	@Transactional(readOnly = true)
	public void refresh(Object entity) {
		hibernateTemplate.refresh(entity);
		
	}

	@Transactional
	@Override
	public void evict(Object entity) {
		hibernateTemplate.evict(entity);
	}
	
	@Transactional
	public void flush() {
		hibernateTemplate.flush();
		hibernateTemplate.clear();
	}
	
	@Transactional
	protected void delete(Object entity) {

		checkDemoMode();

		hibernateTemplate.delete(entity);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> List<T> list(Class<T> cls, boolean caseInsensitive, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> Collection<T> list(Class<T> cls, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> List<T> list(String column, Object value, Class<T> cls, boolean caseInsensitive,
			CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		if (caseInsensitive) {
			criteria.add(Restrictions.eq(column, value).ignoreCase());
		} else {
			criteria.add(Restrictions.eq(column, value));
		}
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		return results;
	}

	@Transactional(readOnly = true)
	protected <T> List<T> list(String column, Object value, Class<T> cls, CriteriaConfiguration... configs) {
		return list(column, value, cls, false, configs);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> T get(String column, Object value, Class<T> cls, boolean caseInsensitive,
			CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		if(StringUtils.isNotBlank(column)) {
			if (caseInsensitive) {
				criteria.add(Restrictions.eq(column, value).ignoreCase());
			} else {
				criteria.add(Restrictions.eq(column, value));
			}
		}
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		if (results.isEmpty()) {
			return null;
		} else if (results.size() > 1) {
			if (log.isWarnEnabled()) {
				log.warn("Too many results returned in get request for column=" + column + " value=" + value + " class="
						+ cls.getName());
			}
		}
		return (T) results.get(0);
	}

	@Transactional(readOnly = true)
	protected <T> T get(String column, Object value, Class<T> cls, CriteriaConfiguration... configs) {
		return get(column, value, cls, false, configs);
	}
	
	@Transactional(readOnly = true)
	protected <T> T get(Class<T> cls, CriteriaConfiguration... configs) {
		return get("", "", cls, false, configs);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> List<T> allEntities(Class<T> cls, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		return (List<T>) criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> List<T> allDeletedEntities(Class<T> cls, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		criteria.add(Restrictions.eq("deleted", true));
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		return (List<T>) criteria.list();
	}

	protected Criteria createCriteria(Class<?> entityClass) {
		return sessionFactory.getCurrentSession().createCriteria(entityClass).setCacheable(true).setCacheMode(CacheMode.NORMAL);
	}

	protected Criteria createCriteria(Class<?> entityClass, String alias) {
		return sessionFactory.getCurrentSession().createCriteria(entityClass, alias).setCacheable(true).setCacheMode(CacheMode.NORMAL);
	}

	@Override
	@Transactional(readOnly = true)
	public Long getCount(Class<?> clz, CriteriaConfiguration... configs) {
		return getCount(clz, "", "", configs);
	}

	@Override
	@Transactional(readOnly = true)
	public Long getCount(Class<?> clz, String searchColumn, String searchPattern, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		Map<String,Criteria> assosications = new HashMap<String,Criteria>();
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		if(StringUtils.isNotBlank(searchPattern) && HibernateUtils.isNotWildcard(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, clz, assosications);
		}

		criteria.setProjection(Projections.rowCount());

		return (long) criteria.uniqueResult();
	}

	@Override
	public List<?> getCounts(Class<?> clz, String groupBy, CriteriaConfiguration... configs) {
		return getCounts(clz, groupBy, false, 0, configs);
	}
	
	@Override
	public List<?> getCounts(Class<?> clz, String groupBy, boolean highestFirst, int maxResults, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		criteria.setProjection(
				Projections.projectionList().add(Projections.groupProperty(groupBy)).add(Projections.count(groupBy).as("resourceCount")));
		
		if(highestFirst) {
			criteria.addOrder(Order.desc("resourceCount"));
		} else {
			criteria.addOrder(Order.asc("resourceCount"));
		}
		
		if(maxResults > 0) {
			criteria.setMaxResults(maxResults);
		}
		return criteria.list();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<?> sum(Class<?> clz, String groupBy, Sort order, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.groupProperty(groupBy));
		projectionList.add(Projections.sum(groupBy), "sum");
		criteria.setProjection(projectionList);
		
		if(order.equals(Sort.DESC)) {
			criteria.addOrder(Order.desc("sum"));
		} else {
			criteria.addOrder(Order.asc("sum"));
		}
		
		return criteria.list();
	}

	@Override
	@Transactional(readOnly = true)
	public Long max(String column, Class<?> clz, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.projectionList().add(Projections.max(column)));

		Integer result = (Integer) criteria.uniqueResult();
		if (result == null) {
			return 0L;
		} else {
			return new Long(result);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long min(String column, Class<?> clz, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.projectionList().add(Projections.min(column)));

		Integer result = (Integer) criteria.uniqueResult();
		if (result == null) {
			return 0L;
		} else {
			return new Long(result);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public <T> List<T> search(Class<T> clz, String searchColumn, String searchPattern, final int start,
			final int length, final ColumnSort[] sorting, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(clz);
		
		Map<String,Criteria> assosications = new HashMap<String,Criteria>();
		
		for (String property : resolveCollectionProperties(clz)) {
			 criteria.setFetchMode(property, org.hibernate.FetchMode.SELECT);
		}
		
		if(StringUtils.isNotBlank(searchPattern) && HibernateUtils.isNotWildcard(searchPattern)) {
			HibernateUtils.configureSearch(searchColumn, searchPattern, criteria, clz, assosications);
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		if(sorting!=null) {
			for (ColumnSort sort : sorting) {
				HibernateUtils.configureSort(sort, criteria, assosications);
			}
		}
		
		criteria.setFirstResult(start);
		criteria.setMaxResults(length);

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		return ((List<T>) criteria.list());
	}
	
	protected List<String> resolveCollectionProperties(Class<?> type) {
		  List<String> ret = new ArrayList<String>();
		  try {
		   BeanInfo beanInfo = Introspector.getBeanInfo(type);
		   for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
		     if (Collection.class.isAssignableFrom(pd.getPropertyType()))
		     ret.add(pd.getName());
		   }
		  } catch (IntrospectionException e) {
		    e.printStackTrace();
		  }
		  return ret;
		}
}
