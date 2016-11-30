package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class PersonalRestriction implements CriteriaConfiguration {

	boolean personal;
	
	public PersonalRestriction(boolean personal) {
		this.personal = personal;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(personal) {
			criteria.add(Restrictions.eq("personal", true));
		} else {
			criteria.add(Restrictions.or(Restrictions.isNull("personal"), Restrictions.eq("personal", false)));
		}
	}

}
