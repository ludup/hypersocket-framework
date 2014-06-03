/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;

@Entity
@Table(name = "auth_schemes")
public class AuthenticationScheme extends Resource {

	@Column(name = "resourceKey")
	String resourceKey;

	@Column(name = "priority")
	Integer priority;

	@OneToOne
	@JoinColumn(name = "realm_id")
	Realm realm;

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String name) {
		this.resourceKey = name;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}
}
