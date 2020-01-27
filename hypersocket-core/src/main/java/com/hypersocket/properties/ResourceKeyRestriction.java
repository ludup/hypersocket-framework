/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class ResourceKeyRestriction implements CriteriaConfiguration {

	private String resourceKey;
	
	public ResourceKeyRestriction(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.eq("resourceKey", resourceKey));

	}

}
