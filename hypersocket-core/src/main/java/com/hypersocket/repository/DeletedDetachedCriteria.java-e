/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class DeletedDetachedCriteria implements CriteriaConfiguration {

	boolean deleted;
	
	public DeletedDetachedCriteria(boolean deleted) {
		this.deleted = deleted;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.eq("deleted", deleted));

	}

}
