/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import com.hypersocket.resource.RealmCriteria;

public class RealmRestriction extends RealmCriteria {
	
	public RealmRestriction(Realm realm) {
		this(realm, "realm");
	}

	public RealmRestriction(Realm realm, String column) {
		super(realm, column);
	}

}
