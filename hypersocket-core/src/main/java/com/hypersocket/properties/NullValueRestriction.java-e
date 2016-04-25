/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class NullValueRestriction implements CriteriaConfiguration {

	String column;
	
	public NullValueRestriction(String column) {
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.isNull(column));
	}

}
