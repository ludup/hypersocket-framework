/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

public class ResourceNotFoundException extends ResourceException {

	private static final long serialVersionUID = -5763235151026115782L;

	public ResourceNotFoundException(String bundle, String resourceKey,
			Object... args) {
		super(bundle, resourceKey, args);
	}
}
