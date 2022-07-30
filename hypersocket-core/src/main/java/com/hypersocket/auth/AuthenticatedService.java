/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Locale;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public interface AuthenticatedService extends Elevatable {

	Principal getCurrentPrincipal();
	
	Session getCurrentSession();
	
	boolean hasAuthenticatedContext();

	String getCurrentUsername();

	Realm getCurrentRealm();

	Locale getCurrentLocale();

	boolean hasSessionContext();

	Role getCurrentRole();

	void setCurrentRole(Role role);

	void setCurrentRole(Session session, Role role);

	boolean hasSystemContext();

	Realm getCurrentRealm(Principal principal);

}
