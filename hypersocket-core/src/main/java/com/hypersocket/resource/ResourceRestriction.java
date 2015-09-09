/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class ResourceRestriction implements CriteriaConfiguration {

	private AbstractResource resource;
	private String column;
	
	public ResourceRestriction(AbstractResource resource) {
		this(resource, "resource");
	}

	public ResourceRestriction(AbstractResource resource, String column) {
		this.resource = resource;
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(resource==null) {
			criteria.add(Restrictions.isNull(column));
		} else {
			criteria.add(Restrictions.eq(column, resource.getId()));
		}
	}

}
