package com.hypersocket.realm;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class PublicRealmCriteria implements CriteriaConfiguration {

	public PublicRealmCriteria() {	
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.or(
							Restrictions.and(Restrictions.isNull("publicRealm"), Restrictions.isNull("owner")),
							Restrictions.eq("publicRealm", Boolean.TRUE)));
	}

}
