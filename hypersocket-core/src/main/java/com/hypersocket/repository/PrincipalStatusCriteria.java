/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.PrincipalStatus;

public class PrincipalStatusCriteria implements CriteriaConfiguration {

	private PrincipalStatus[] status;
	
	public PrincipalStatusCriteria(PrincipalStatus...status) {
		this.status = status;
	}
	
	@Override
	public void configure(Criteria criteria) {
		
		criteria.add(Restrictions.in("status", status));

	}

}
