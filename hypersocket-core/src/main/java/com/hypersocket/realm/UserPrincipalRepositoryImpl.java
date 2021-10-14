package com.hypersocket.realm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.HibernateUtils;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.utils.HypersocketUtils;

@Repository
public class UserPrincipalRepositoryImpl extends AbstractResourceRepositoryImpl<UserPrincipal<GroupPrincipal<?,?>>> implements UserPrincipalRepository {
	
	final static Logger LOG = LoggerFactory.getLogger(UserPrincipalRepositoryImpl.class);
	
	@SuppressWarnings("unchecked")
	@Override
	protected Class<UserPrincipal<GroupPrincipal<?, ?>>> getResourceClass() {
		return (Class<UserPrincipal<GroupPrincipal<?, ?>>>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractResourceRepositoryImpl.class);
	}
	

	@Override
	@Transactional(readOnly = true)
	public List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		
		return super.search(realm, searchColumn, searchPattern, start, length, sorting, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.isNull("lastSignOn"))
						.add(Restrictions.eq("hidden", false));
			}
		});
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getNeverLoggedInCount(Realm realm, String searchColumn, String searchPattern) {
		
		return searchCount(UserPrincipal.class, searchColumn, searchPattern, new RealmCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.isNull("lastSignOn"))
						.add(Restrictions.eq("hidden", false));
			}
		});
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInDaysSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, int days) {

		return super.search(realm, searchColumn, searchPattern, start, length, sorting, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.conjunction().add(Restrictions.isNotNull("lastSignOn"))
						.add(Restrictions.lt("lastSignOn", DateUtils.addDays(HypersocketUtils.today(), -days)))
						.add(Restrictions.eq("hidden", false))
				);
			}
		});
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getNeverLoggedInDaysCount(Realm realm, String searchColumn, String searchPattern, int days) {
		
		return searchCount(UserPrincipal.class, searchColumn, searchPattern, new RealmCriteria(realm), new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.conjunction().add(Restrictions.isNotNull("lastSignOn"))
						.add(Restrictions.lt("lastSignOn", DateUtils.addDays(HypersocketUtils.today(), -days)))
						.add(Restrictions.eq("hidden", false))
				);
			}
		});
		
	}
	
	/**
	 * UserPrincipal is abstract class count returns result for each implementation hence summing them all.
	 */
	@SuppressWarnings("unchecked")
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

		List<Long> results = criteria.list();
		
		if(!results.isEmpty()) {
			Long sum = 0l;
			for (Long result : results) {
				sum += result;
			}
			return sum;
		}
		return 0L;
	}
}
