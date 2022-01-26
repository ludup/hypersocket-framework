/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Locale;
import java.util.concurrent.Callable;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
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

	Role getCurrentRole();

	void setCurrentRole(Role role);

	void setCurrentRole(Session session, Role role);

	boolean hasSystemContext();

	void setupSystemContext();
	
	void setupSystemContext(Realm realm);

	void setupSystemContext(Principal principal);

	Realm getCurrentRealm(Principal principal);
	
	default <T> T callAsSystemContext(Callable<T> callable) throws Exception {
		setupSystemContext();
		try {
			return callable.call();
		}
		finally {
			clearPrincipalContext();
		}	
	} 
	
	default void runAsSystemContext(Runnable r) {
		setupSystemContext();
		try {
			r.run();
		}
		finally {
			clearPrincipalContext();
		}
	}

}
