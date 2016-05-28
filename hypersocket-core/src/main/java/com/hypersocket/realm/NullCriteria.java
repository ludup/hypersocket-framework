package com.hypersocket.realm;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class NullCriteria implements CriteriaConfiguration {

	String columnName = "resource";
	
	public NullCriteria() {	
	}
	
	public NullCriteria(String columnName) {
		this.columnName = columnName;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.isNull(columnName));
	}

}
