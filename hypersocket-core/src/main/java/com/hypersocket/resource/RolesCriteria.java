package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.permissions.Role;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.repository.CriteriaConfiguration;

public class RolesCriteria implements CriteriaConfiguration {

	Role[] roles;
	public RolesCriteria(Role... roles) {
		this.roles = roles;
	}

	@Override
	public void configure(Criteria criteria) {
		criteria = criteria.createCriteria("roles");
		criteria.add(Restrictions.in("id", ResourceUtils.createResourceIdArray(roles)));
	}

}
