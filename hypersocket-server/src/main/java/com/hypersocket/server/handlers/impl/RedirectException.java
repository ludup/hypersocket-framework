/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

public class RedirectException extends Exception {

	private static final long serialVersionUID = 8821545531151846549L;
	
	private boolean permanent;
	
	public RedirectException(String location) {
		super(location);
	}
	
	public RedirectException(String location, boolean permanent) {
		super(location);
		this.permanent = permanent;
	}

	public boolean isPermanent() {
		return permanent;
	}
}
