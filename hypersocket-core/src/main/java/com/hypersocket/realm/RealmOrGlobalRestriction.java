/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.DetachedCriteriaConfiguration;

public class RealmOrGlobalRestriction implements DetachedCriteriaConfiguration {

	private Realm realm;
	private String column;
	
	public RealmOrGlobalRestriction(Realm realm) {
		this(realm, "realm");
	}

	public RealmOrGlobalRestriction(Realm realm, String column) {
		this.realm = realm;
		this.column = column;
	}
	
	@Override
	public void configure(DetachedCriteria criteria) {
		criteria.add(Restrictions.or(Restrictions.eq(column, realm), Restrictions.isNull(column)));
	}

}
