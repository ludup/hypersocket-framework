package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;

public class RealmCriteria implements CriteriaConfiguration {

	Realm realm;
	String column = "realm";
	public RealmCriteria(Realm realm) {
		this.realm = realm;
	}
	
	public RealmCriteria(Realm realm, String column) {
		this.realm = realm;
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(realm==null) {
			criteria.add(Restrictions.isNull(column));
		} else {
			criteria.add(Restrictions.eq(column, realm));
		}
	}

}
