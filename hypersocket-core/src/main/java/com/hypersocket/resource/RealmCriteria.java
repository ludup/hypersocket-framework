package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;

public class RealmCriteria implements CriteriaConfiguration {

	Realm realm;
	public RealmCriteria(Realm realm) {
		this.realm = realm;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(realm==null) {
			criteria.add(Restrictions.isNull("realm"));
		} else {
			criteria.add(Restrictions.eq("realm", realm));
		}
	}

}
