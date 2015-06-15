/*******************************************************************************
\ * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.Locale;

import com.hypersocket.i18n.I18N;

public class ResourceException extends Exception {

	private static final long serialVersionUID = 8760287136428990927L;


	String bundle;
	String resourceKey;
	Object[] args;
	
	public ResourceException(String bundle, String resourceKey, Object... args) {
		super(I18N.getResource(Locale.getDefault(), bundle, resourceKey, args));
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
