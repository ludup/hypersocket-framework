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

	public AuthenticationSuccessResult() {

	}

	public AuthenticationSuccessResult(String bannerMsg, boolean showLocales, Session session) {
		super(bannerMsg, null, showLocales);
		this.session = session;
		setSuccess(true);
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
