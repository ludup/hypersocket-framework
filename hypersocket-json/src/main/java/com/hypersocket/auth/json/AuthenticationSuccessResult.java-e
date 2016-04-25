/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.session.Session;

public class AuthenticationSuccessResult extends AuthenticationResult {

	Session session;
	String homePage;
	
	public AuthenticationSuccessResult() {

	}

	public AuthenticationSuccessResult(String bannerMsg, boolean showLocales, Session session, String homePage) {
		super(bannerMsg, null, showLocales);
		this.session = session;
		this.homePage = homePage;
		setSuccess(true);
	}

	public Session getSession() {
		return session;
	}
	
	public String getHomePage() {
		return homePage;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
