/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class RealmRestriction implements CriteriaConfiguration {

	private Realm realm;
	private String column;
	
	public RealmRestriction(Realm realm) {
		this(realm, "realm");
	}

	public RealmRestriction(Realm realm, String column) {
		this.realm = realm;
		this.column = column;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(realm==null) {
			criteria.add(Restrictions.isNull(column));
		} else {
			criteria.add(Restrictions.eq(column, realm));
		}
	}

}
