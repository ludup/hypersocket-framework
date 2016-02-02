package com.hypersocket.browser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

@Repository
public class BrowserLaunchableRepositoryImpl extends
		AbstractRepositoryImpl<Long> implements BrowserLaunchableRepository {


	@Override
	@Transactional(readOnly=true)
	public List<BrowserLaunchable> searchAssignedResources(List<Principal> principals,
			final String searchPattern, final int start, final int length,
			final ColumnSort[] sorting, CriteriaConfiguration... configs) {


		/**
		 * There appears to be no way around this. DISTINCT_ROOT_ENTITY is
		 * returning max results * number of implementations of BrowserLaunchable
		 * so instead get them all and chop the collection to the required
		 * length and starting position.
		 */
		List<BrowserLaunchable> resources = getPersonalResources(principals);
		return resources.subList(start, Math.min(start+length, resources.size()));
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
		
		/**
		 * Get any resources assigned to Everyone role.
		 */
		Criteria criteria = createCriteria(BrowserLaunchable.class);

		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", true));
		
		HashSet<Long> allResources = new HashSet<Long>(criteria.list());

		/**
		 * Now look for additional resources attached to any Role that has
		 * any of the principals listed.
		 */
		criteria = createCriteria(BrowserLaunchable.class);
		
		criteria.setProjection(Projections.distinct(Projections.id()));
		criteria.setResultTransformer(CriteriaSpecification.PROJECTION);
		
		criteria.add(Restrictions.eq("realm", principals.get(0).getRealm()));
		
		criteria.add(Restrictions.or(
				Restrictions.eq("displayInBrowserResourcesTable", true), 
				Restrictions.isNull("displayInBrowserResourcesTable")));
		
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.eq("allUsers", false));
		criteria = criteria.createCriteria("principals");
		
		List<Long> ids = new ArrayList<Long>();
		for(Principal p : principals) {
			ids.add(p.getId());
		}
		criteria.add(Restrictions.in("id", ids));
		
		allResources.addAll(criteria.list());
		
		/**
		 * Now return all the distinct resources based on the unique set of 
		 * resource ids.
		 */
		if(allResources.size() > 0) {
						
			criteria = createCriteria(BrowserLaunchable.class);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			
			criteria.add(Restrictions.in("id", allResources));
	
			return criteria.list();
		}
		
		return new ArrayList<BrowserLaunchable>();
	}

}
