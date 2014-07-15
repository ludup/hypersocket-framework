/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Repository
@Transactional
public abstract class AbstractRepositoryImpl<K> implements AbstractRepository<K> {

	protected HibernateTemplate hibernateTemplate;
	protected SessionFactory sessionFactory;

	protected AbstractRepositoryImpl() {

	}
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
	    hibernateTemplate = new HibernateTemplate(this.sessionFactory = sessionFactory);
	}
	
	protected DetachedCriteria createDetachedCriteria(Class<?> entityClass) {
		return DetachedCriteria.forClass(entityClass);
	}
	
	protected void saveObject(Object entity) {
		hibernateTemplate.save(entity);
	}
	
	protected void save(AbstractEntity<K> entity) {
		
		entity.setLastModified(new Date());
		
		if(entity.getId()!=null) {
			hibernateTemplate.merge(entity);
		} else {
			hibernateTemplate.saveOrUpdate(entity);
		}
	}
	
	public void refresh(Object entity) {
		hibernateTemplate.refresh(entity);
	}
	
	public void flush() {
		hibernateTemplate.flush();
		hibernateTemplate.clear();
	}
	
	protected void delete(Object entity) {
		hibernateTemplate.delete(entity);
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> list(Class<T> cls, boolean caseInsensitive, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> list(String column, Object value, Class<T> cls, boolean caseInsensitive, DetachedCriteriaConfiguration... configs) {
		DetachedCriteria criteria = createDetachedCriteria(cls);
		if(caseInsensitive) {
			criteria.add(Restrictions.eq(column, value).ignoreCase());
		} else {
			criteria.add(Restrictions.eq(column, value));
		}
		for(DetachedCriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		@SuppressWarnings("rawtypes")
		List results = hibernateTemplate.findByCriteria(criteria);
		return results;
	}
	
	protected <T> List<T> list(String column, Object value, Class<T> cls, DetachedCriteriaConfiguration... configs) {
		return list(column, value, cls, false, configs);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T get(String column, Object value, Class<T> cls, boolean caseInsensitive, DetachedCriteriaConfiguration... configs) {
		DetachedCriteria criteria = createDetachedCriteria(cls);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		if(caseInsensitive) {
			criteria.add(Restrictions.eq(column, value).ignoreCase());
		} else {
			criteria.add(Restrictions.eq(column, value));
		}
		for(DetachedCriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		@SuppressWarnings("rawtypes")
		List results = hibernateTemplate.findByCriteria(criteria);
		if(results.isEmpty()) {
			return null;
		} else if(results.size() > 1) {
			throw new IllegalStateException("Too many results returned in get request for column=" + column + " value=" + value + " class=" + cls.getName());
		}
		return (T)results.get(0);
	}
	
	protected <T> T get(String column, Object value, Class<T> cls, DetachedCriteriaConfiguration... configs) {
		return get(column, value, cls, false, configs);
	}
	
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> allEntities(Class<T> cls, DetachedCriteriaConfiguration... configs) {
		DetachedCriteria criteria = createDetachedCriteria(cls);
		criteria.add(Restrictions.eq("deleted", false));
		for(DetachedCriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		return (List<T>)hibernateTemplate.findByCriteria(criteria);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> allEntities(Class<T> cls, CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(cls);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		return (List<T>)criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> allDeletedEntities(Class<T> cls, DetachedCriteriaConfiguration... configs) {
		DetachedCriteria criteria = createDetachedCriteria(cls);
		criteria.add(Restrictions.eq("deleted", true));
		for(DetachedCriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		return (List<T>)hibernateTemplate.findByCriteria(criteria);
	}
	
	protected Criteria createCriteria(Class<?> entityClass) {
		return sessionFactory.getCurrentSession().createCriteria(entityClass);
	}

	@Override 
	public Long getCount(Class<?> clz, CriteriaConfiguration... configs) {
		return getCount(clz, "", "", configs);
	}
	
	@Override
	public Long getCount(Class<?> clz, String searchColumn, String searchPattern, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(clz);
		
		criteria.setProjection(Projections.rowCount());
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		if(!StringUtils.isEmpty(searchPattern)) {
			criteria.add(Restrictions.like(searchColumn, searchPattern));
		}
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		
		return (Long) criteria.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> search(Class<T> clz, 
			final String searchColumn, 
			final String searchPattern, 
			final int start,
			final int length, 
			final ColumnSort[] sorting, 
			CriteriaConfiguration... configs) {
		Criteria criteria = createCriteria(clz);
		if(!StringUtils.isEmpty(searchPattern)) {
			criteria.add(Restrictions.like(searchColumn, searchPattern));
		}
	
		for (ColumnSort sort : sorting) {
			criteria.addOrder(sort.getSort() == Sort.ASC ? Order
					.asc(sort.getColumn().getColumnName()) : Order.desc(sort.getColumn().getColumnName()));
		}
		for(CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	
		criteria.setFirstResult(start);
		criteria.setMaxResults(length);
		
		List<T> res = (List<T>)criteria.list();
		return res;
	};
}
