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

	String bundle;
	String resourceKey;
	Object[] args;

	public ResourceNotFoundException(String bundle, String resourceKey,
			Object... args) {
		super((String) null);
		this.bundle = bundle;
		this.resourceKey = resourceKey;
		this.args = args;
	}

	public String getBundle() {
		return bundle;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public Object[] getArgs() {
		return args;
	}

}
