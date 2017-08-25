package com.hypersocket.resource;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;

public class RealmsCriteria implements CriteriaConfiguration {

	Collection<Realm> realms;
	String column = "realm";
	public RealmsCriteria(Collection<Realm> realms) {
		this.realms = realms;
	}
	
	public RealmsCriteria(Collection<Realm> realms, String column) {
		this.realms = realms;
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.createAlias(column, "r");
		if(realms==null) {
			criteria.add(Restrictions.eq("r.deleted", false));
		} else {
			criteria.add(Restrictions.in("r.id", ResourceUtils.createResourceIdArray(realms)));
			criteria.add(Restrictions.eq("r.deleted", false));
		}
	}

}
