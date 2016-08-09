/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LocalRealmProviderImpl extends AbstractLocalRealmProviderImpl {

	private static Logger log = LoggerFactory
			.getLogger(LocalRealmProviderImpl.class);

	public final static String REALM_RESOURCE_CATEGORY = "local";

	@Override
	public String getModule() {
		return REALM_RESOURCE_CATEGORY;
	}


}
