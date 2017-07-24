package com.hypersocket.profile;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.RealmCriteria;

@Repository
public class ProfileRepositoryImpl extends AbstractEntityRepositoryImpl<Profile, Long> implements ProfileRepository {

	@Override
	protected Class<Profile> getEntityClass() {
		return Profile.class;
	}

	
	@Override
	@Transactional(readOnly=true)
	public long getCompleteProfileCount(Realm realm) {
		return getCount(Profile.class, new RealmCriteria(realm.isSystem() ? null : realm), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.COMPLETE));
			} 
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getIncompleteProfileCount(Realm realm) {
		return getCount(Profile.class, new RealmCriteria(realm.isSystem() ? null : realm), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.INCOMPLETE));
			} 
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getPartiallyCompleteProfileCount(Realm realm) {
		return getCount(Profile.class, new RealmCriteria(realm.isSystem() ? null : realm), new CriteriaConfiguration(){

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("state", ProfileCredentialsState.PARTIALLY_COMPLETE));
			} 
		});
	}


	@Override
	public Collection<Profile> getPrincipalsWithProfileStatus(Realm realm, final ProfileCredentialsState...credentialsStates) {
		return list(Profile.class, new RealmCriteria(realm), new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.in("state", credentialsStates));
			}
		});
	}
}
