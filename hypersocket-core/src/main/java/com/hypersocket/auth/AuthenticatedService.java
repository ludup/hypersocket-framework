/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Locale;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public interface AuthenticatedService {

	Principal getCurrentPrincipal();
	
	Session getCurrentSession();

	void clearPrincipalContext();
	
	boolean hasAuthenticatedContext();

	String getCurrentUsername();

	Realm getCurrentRealm();

	Locale getCurrentLocale();

	boolean hasSessionContext();

	void elevatePermissions(PermissionType... permissions);

	void clearElevatedPermissions();

	void setCurrentSession(Session session, Locale locale);
	
	void setCurrentSession(Session session, Realm realm, Locale locale);

	void setCurrentSession(Session session, Realm realm, Principal principal, Locale locale);

	boolean isSystemContext();

//	void setCurrentPrincipal(Principal principal, Locale locale, Realm realm);
}
