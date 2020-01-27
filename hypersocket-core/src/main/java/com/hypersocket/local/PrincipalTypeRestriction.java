/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.realm.PrincipalType;
import com.hypersocket.repository.CriteriaConfiguration;

public class PrincipalTypeRestriction implements CriteriaConfiguration {

	private PrincipalType type;
	
	public PrincipalTypeRestriction(PrincipalType type) {
		this.type = type;
	}
	
	@Override
	public void configure(Criteria criteria) {
		criteria.add(Restrictions.eq("principalType", type));
	}

}
