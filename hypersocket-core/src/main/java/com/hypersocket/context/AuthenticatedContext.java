/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.session.Session;

/**
 * Used to elevate a methods invocation current {@link Session}. When no options are present,
 * the currently authenticated user will be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthenticatedContext  {

	/**
	 * Create a new 
	 * @return
	 */
	boolean system() default false;
	
	/**
	 * Create a session for the realm, which may be decided by the hostname in the request.
	 * 
	 * @return realm host
	 */
	boolean realmHost() default false;
	
	/**
	 * Use either the current realm session, or a default session if non is available.
	 * 
	 * @return current realm or default
	 */
	boolean currentRealmOrDefault() default false;

	/**
	 * Prefer the currently active session, but fallback to a system session if not available.
	 * 
	 * @return prefer active
	 */
	boolean preferActive() default false;

	/**
	 * Requests that at least an anonymous session is availabe.
	 * 
	 * @return anonymous
	 */
	boolean anonymous() default false;
	
	/**
	 * Requests that the session be elevated to use obtained from the current {@link AuthenticationState}, i.e.
	 * the user that is actually logged in.
	 * 
	 * @return use current principal
	 */
	boolean principal() default false;
}
