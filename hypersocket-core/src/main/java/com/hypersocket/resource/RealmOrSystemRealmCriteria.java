package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;

public class RealmOrSystemRealmCriteria implements CriteriaConfiguration {

	Realm realm;
	String column = "realm";
	public RealmOrSystemRealmCriteria(Realm realm) {
		this.realm = realm;
	}
	
	public RealmOrSystemRealmCriteria(Realm realm, String column) {
		this.realm = realm;
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(realm!=null && !realm.isSystem()) {
			criteria.add(Restrictions.eq(column, realm));
		}
	}

}
