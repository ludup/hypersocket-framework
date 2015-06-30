/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.attributes.user.UserAttributeService;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.RealmProvider;


public abstract class AbstractRealmProvider extends ResourceTemplateRepositoryImpl implements RealmProvider {


	@Autowired
	protected UserAttributeService userAttributeService;
	
}
