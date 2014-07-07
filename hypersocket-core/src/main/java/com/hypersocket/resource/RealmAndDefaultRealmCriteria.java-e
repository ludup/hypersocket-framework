package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;

public class RealmAndDefaultRealmCriteria implements CriteriaConfiguration {

	Realm realm;
	public RealmAndDefaultRealmCriteria(Realm realm) {
		this.realm = realm;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.or(Restrictions.eq("realm", realm), Restrictions.isNull("realm")));
	}

}
