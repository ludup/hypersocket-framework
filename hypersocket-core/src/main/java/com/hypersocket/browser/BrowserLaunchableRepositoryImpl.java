package com.hypersocket.browser;

import java.util.ArrayList;
import java.util.List;

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
	public List<BrowserLaunchable> searchAssignedResources(Principal principal,
			final String searchPattern, final int start, final int length,
			final ColumnSort[] sorting, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(BrowserLaunchable.class);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.like("name", searchPattern));
		}

		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", principal.getRealm()));
		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<BrowserLaunchable> everyone = new ArrayList<BrowserLaunchable>(criteria.list());

		criteria = createCriteria(BrowserLaunchable.class);
		
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"));
		projList.add(Projections.property("name"));
		
		criteria.setProjection(Projections.distinct(projList));
		criteria.setFirstResult(start);
		criteria.setMaxResults(length);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.like("name", searchPattern));
		}

		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		for (ColumnSort sort : sorting) {
			criteria.addOrder(sort.getSort() == Sort.ASC ? Order.asc(sort
					.getColumn().getColumnName()) : Order.desc(sort.getColumn()
					.getColumnName()));
		}
		
		criteria.add(Restrictions.eq("realm", principal.getRealm()));

		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		criteria.add(Restrictions.in("id", new Long[] { principal.getId() }));
		
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
	};

	@Override
	@Transactional(readOnly=true)
	public Long getAssignedResourceCount(Principal principal,
			final String searchPattern, CriteriaConfiguration... configs) {

		Criteria criteria = createCriteria(BrowserLaunchable.class);
		criteria.setProjection(Projections.property("id"));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.like("name", searchPattern));
		}

		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}

		criteria.add(Restrictions.eq("realm", principal.getRealm()));
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<?> ids = criteria.list();
		if(ids==null) {
			ids = new ArrayList<>();
		}
		criteria = createCriteria(BrowserLaunchable.class);
		criteria.setProjection(Projections.countDistinct("name"));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.like("name", searchPattern));
		}

		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		for (CriteriaConfiguration c : configs) {
			c.configure(criteria);
		}
		
		criteria.add(Restrictions.eq("realm", principal.getRealm()));
		if(ids.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("id", ids)));
		}
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		criteria.add(Restrictions.in("id", new Long[] { principal.getId() }));
		
		@SuppressWarnings("unchecked")
		List<Long> list = (List<Long>)criteria.list();
		
		if(list.isEmpty()) {
			return 0L;
		}
		
		long count = 0L;
		for(Long l : list) {
			count += l;
		}
		
		return count;

	}

	@Override
	@Transactional(readOnly=true)
	public List<BrowserLaunchable> getPersonalResources(Principal principal) {
		
		Criteria criteria = createCriteria(BrowserLaunchable.class);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		criteria.add(Restrictions.eq("realm", principal.getRealm()));
		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		List<BrowserLaunchable> everyone = new ArrayList<BrowserLaunchable>(criteria.list());

		criteria = createCriteria(BrowserLaunchable.class);
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"));
		projList.add(Projections.property("name"));
		
		criteria.setProjection(Projections.distinct(projList));
		criteria.add(Restrictions.eq("realm", principal.getRealm()));
		criteria.add(Restrictions.eq("displayInBrowserResourcesTable", true));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		criteria.add(Restrictions.in("id", new Long[] { principal.getId() }));
		
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
