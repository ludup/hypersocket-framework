package com.hypersocket.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class SystemRestriction implements CriteriaConfiguration {

	boolean wantSystem = true;
	
	public SystemRestriction(boolean wantSystem) {
		this.wantSystem = wantSystem;
	}
	
	public SystemRestriction() {
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.eq("system", wantSystem));
	}

}
