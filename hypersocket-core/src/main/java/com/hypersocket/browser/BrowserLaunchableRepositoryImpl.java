package com.hypersocket.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Repository
public class BrowserLaunchableRepositoryImpl extends
		AbstractRepositoryImpl<Long> implements BrowserLaunchableRepository {


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<BrowserLaunchable> searchAssignedResources(List<Principal> principals,
			final String searchPattern, final int start, final int length,
			final ColumnSort[] sorting, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(BrowserLaunchable.class);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());

		
		criteria = createCriteria(BrowserLaunchable.class);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);

		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));

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
			
			criteria.add(Restrictions.in("id", ids));

			criteria.setFirstResult(start);
			criteria.setMaxResults(length);
			
			for (ColumnSort sort : sorting) {
				criteria.addOrder(sort.getSort() == Sort.ASC ? Order.asc(sort
						.getColumn().getColumnName()) : Order.desc(sort.getColumn()
						.getColumnName()));
			}
			
			return ((List<BrowserLaunchable>) criteria.list());
		}
		
		return new ArrayList<BrowserLaunchable>();
	};


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Long getAssignedResourceCount(List<Principal> principals,
			final String searchPattern, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(BrowserLaunchable.class);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));


		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		Set<Long> ids = new HashSet<Long>(criteria.list());

		
		criteria = createCriteria(BrowserLaunchable.class);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));


		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));

		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> principalIds = new ArrayList<Long>();
		for(Principal p : principals) {
			principalIds.add(p.getId());
		}
		criteria.add(Restrictions.in("id", principalIds));
		
		ids.addAll(criteria.list());
		
		return new Long(ids.size());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<BrowserLaunchable> getPersonalResources(List<Principal> principals) {
		
		Criteria criteria = createCriteria(BrowserLaunchable.class);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<BrowserLaunchable> everyone = new ArrayList<BrowserLaunchable>(criteria.list());

		criteria = createCriteria(BrowserLaunchable.class);
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"));
		projList.add(Projections.property("name"));
		
		criteria.setProjection(Projections.distinct(projList));
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> ids = new ArrayList<Long>();
		for(Principal p : principals) {
			ids.add(p.getId());
		}
		criteria.add(Restrictions.in("id", ids));
		
		List<Object[]> results = (List<Object[]>)criteria.list();
		
		if(results.size() > 0) {
			Long[] entityIds = new Long[results.size()];
			int idx = 0;
			for(Object[] obj : results) {
				entityIds[idx++] = (Long) obj[0];
			}
			
			criteria = createCriteria(BrowserLaunchable.class);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			
			criteria.add(Restrictions.in("id", entityIds));
	
			everyone.addAll((List<BrowserLaunchable>) criteria.list());
		}
		return everyone;
	}

}
