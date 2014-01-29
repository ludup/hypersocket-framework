/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

public class ResourceCreationException extends ResourceException {

	private static final long serialVersionUID = 7411495296618455618L;

	String bundle;
	String resourceKey;
	Object[] args;
	
	public ResourceCreationException(String bundle, String resourceKey, Object... args) {
		super((String)null);
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
