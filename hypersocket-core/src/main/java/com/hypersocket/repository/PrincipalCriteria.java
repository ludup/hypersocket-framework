package com.hypersocket.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.Principal;

public class PrincipalCriteria implements CriteriaConfiguration {

	Principal principal;
	public PrincipalCriteria(Principal principal) {
		this.principal = principal;
	}
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.eq("principal", principal));
	}

}
