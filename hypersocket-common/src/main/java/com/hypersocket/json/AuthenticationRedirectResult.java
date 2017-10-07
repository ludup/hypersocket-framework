/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

public class AuthenticationRedirectResult extends AuthenticationResult {

	String location;

	public AuthenticationRedirectResult() {
	}

	public AuthenticationRedirectResult(String bannerMsg, String errorMsg, boolean showLocales, String location) {
		super(bannerMsg, errorMsg, showLocales);
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
