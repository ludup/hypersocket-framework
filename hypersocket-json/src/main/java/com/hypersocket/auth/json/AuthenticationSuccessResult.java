/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth.json;

import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.permissions.Role;
import com.hypersocket.session.Session;

public class AuthenticationSuccessResult extends AuthenticationResult {

	private Session session;
	private Role currentRole;
	private String homePage;
	
	public AuthenticationSuccessResult() {

	}

	public AuthenticationSuccessResult(String error, String errorStyle, boolean showLocales, Session session, String homePage, Role currentRole) {
		super(null, error, errorStyle, showLocales);
		this.session = session;
		this.homePage = homePage;
		this.currentRole = currentRole;
		setSuccess(true);
	}

	public AuthenticationSuccessResult(String bannerMsg, boolean showLocales, Session session, String homePage, Role currentRole) {
		super(bannerMsg, null, null, showLocales);
		this.session = session;
		this.homePage = homePage;
		this.currentRole = currentRole;
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

	public Role getCurrentRole() {
		return currentRole;
	}
	
	
}
