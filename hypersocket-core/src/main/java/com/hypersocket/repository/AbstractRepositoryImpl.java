/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;
import com.hypersocket.util.PagedIterator;

@Repository
public abstract class AbstractRepositoryImpl<K> implements AbstractRepository<K> {

	static Logger log = LoggerFactory.getLogger(AbstractRepositoryImpl.class);

	
	private static ThreadLocal<Boolean> cache = new ThreadLocal<>();
	private static ThreadLocal<String> cacheRegion = new ThreadLocal<>();

	private HibernateTemplate hibernateTemplate;	
	private boolean requiresDemoWrite = false;
	
	protected SessionFactory sessionFactory;

	protected AbstractRepositoryImpl() {

	}

	protected AbstractRepositoryImpl(boolean requiresDemoWrite) {
		this.requiresDemoWrite = requiresDemoWrite;
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		hibernateTemplate = new HibernateTemplate(this.sessionFactory = sessionFactory);
		hibernateTemplate.setCacheQueries(isCache());
	}

	private void checkDemoMode() {
		if (Boolean.getBoolean("hypersocket.demo") && !requiresDemoWrite) {
			throw new IllegalStateException("This is a demo. No changes to resources or settings can be persisted.");
		}
	}

	protected Session getCurrentSession() {
		return hibernateTemplate.getSessionFactory().getCurrentSession();
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public <I> Iterator<I> iterate(Class<I> clazz, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return new PagedIterator<I>(sorting) { 
			@Override
			protected List<I> listItems(int start, int length, ColumnSort[] sorting) {
				return search(clazz, null, null, start, length, sorting, configs);
			}
			
			@Override
			protected void remove(I principal) {
				delete(principal);
			}
			
		};
	}
	
	protected HibernateTemplate getTemplate() {
		return hibernateTemplate;
	}
	
	@Transactional
	protected AbstractEntity<K> save(AbstractEntity<K> entity) {

		checkDemoMode();

		if(!entity.isPreserveTimestamp()) {
			entity.setModifiedDate(new Date());
		}

		if(entity.getId() != null) {
			entity = hibernateTemplate.merge(entity);
		} else {
			hibernateTemplate.saveOrUpdate(entity);
		}
		
		return entity;
	}
	
	protected void saveObject(Object entity) {
		hibernateTemplate.saveOrUpdate(entity);
	}

	@Transactional
	protected void saveEntities(Collection<AbstractEntity<K>> resources) {
		for(AbstractEntity<K> resource : resources) {
			save(resource);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	protected void save(Object entity, boolean isNew) {

		checkDemoMode();

		if(entity instanceof AbstractEntity<?>) {
			if(!((AbstractEntity<?>)entity).isPreserveTimestamp()) {
				((AbstractEntity<?>)entity).setModifiedDate(new Date());
			}
		}
		if(!isNew) {
			hibernateTemplate.merge(entity);
		} else {
			hibernateTemplate.saveOrUpdate(entity);
		}
	}

	@Override
	public void assosicate(Object entity) {
		hibernateTemplate.saveOrUpdate(entity);
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
	
	protected Query createSQLQuery(String hql, boolean isWritable) {

		if (isWritable) {
			checkDemoMode();
		}
		return sessionFactory.getCurrentSession().createSQLQuery(hql);
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
	@Override
	public Object merge(Object entity) {
		return hibernateTemplate.merge(entity);
	}
	
	@Transactional
	public void flush() {
		hibernateTemplate.flush();
		hibernateTemplate.clear();
	}
	
	@Transactional
	protected void delete(Object entity) {

		checkDemoMode();

		hibernateTemplate.delete(hibernateTemplate.merge(entity));
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
	protected <T> List<T> query(String query, Object... args) {
		Query q = buildQuery(query, args);
		return q.list();
	}

	protected Query buildQuery(String query, Object... args) {
		Query q = createQuery(query, false);
		for(int i = 0 ; i < args.length; i++) {
			q.setParameter(i, args[i]);
		}
		return q;
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

	@Transactional(readOnly = true)
	protected <T> T get(String column, Object value, Class<T> cls, 
			CriteriaConfiguration... configs) {
		return get(column, value, cls, false, configs);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	protected <T> T get(String column, Object value, Class<T> cls, boolean caseInsensitive,
			CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		if(StringUtils.isNotBlank(column)) {
			if (caseInsensitive && HibernateUtils.isString(cls, column)) {
				criteria.add(Restrictions.eq(column,  StringUtils.defaultString((String)value)).ignoreCase());
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
	
	public static boolean setCache(boolean cache) {
		boolean was = isCache();
		AbstractRepositoryImpl.cache.set(cache);
		return was;
	}
	
	public static String setCacheRegion(String cacheRegion) {
		String was = getCacheRegion();
		AbstractRepositoryImpl.cacheRegion.set(cacheRegion);
		return was;
	}
	
	public static String getCacheRegion() {
		return AbstractRepositoryImpl.cacheRegion.get();
	}

	
	public static boolean isCache() {
		return !Boolean.FALSE.equals(AbstractRepositoryImpl.cache.get());
	}

	protected Criteria createCriteria(Class<?> entityClass) {
		return configureCriteriaCaching(sessionFactory.getCurrentSession().createCriteria(entityClass));
	}

	protected Criteria configureCriteriaCaching(final Criteria crit) {
		if(isCache()) {
			crit.setCacheable(true);
			crit.setCacheMode(CacheMode.NORMAL);
			final String region = cacheRegion.get();
			if(StringUtils.isNotBlank(region))
				crit.setCacheRegion(region);
		}
		else {
			crit.setCacheable(false);
			crit.setCacheMode(CacheMode.IGNORE);
		}
		return crit;
	}

	protected Criteria createCriteria(Class<?> entityClass, String alias) {
		return configureCriteriaCaching(sessionFactory.getCurrentSession().createCriteria(entityClass, alias));
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

		Object result = criteria.uniqueResult();
		if(result!=null) {
			return (Long) result;
		}
		return 0L;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getDistinctCount(Class<?> clz, String distinctColumn, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setProjection(Projections.countDistinct(distinctColumn));

		Object result = criteria.uniqueResult();
		if(result!=null) {
			return (Long) result;
		}
		return 0L;
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
	public List<?> total(Class<?> clz, String column, Sort order, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.sum(column), "total");
		criteria.setProjection(projectionList);
		
		if(order.equals(Sort.DESC)) {
			criteria.addOrder(Order.desc("total"));
		} else {
			criteria.addOrder(Order.asc("total"));
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
	@Override
	public <T> List<T> search(Class<T> clz, String searchColumn, String searchPattern, final int start,
			final int length, final ColumnSort[] sorting, CriteriaConfiguration... configs) {
		
		if(length < 0) {
			throw new IllegalArgumentException("Search length cannot be negative");
		} 
		if(length == 0) {
			return new ArrayList<T>();
		}
		
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
	
	@Override
	@Transactional(readOnly = true)
	public <T> long searchCount(Class<T> clz, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		
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

		criteria.setProjection(Projections.rowCount());

		Object result = criteria.uniqueResult();
		if(result!=null) {
			return (Long) result;
		}
		return 0L;
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
