/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import org.jboss.netty.handler.codec.http.Cookie;

public class CookieHolder {

	private Cookie cookie;
	private long lastUpdated = System.currentTimeMillis();
	
	public CookieHolder(Cookie cookie) {
		this.cookie = cookie;
	}
	
	public Cookie getCookie() {
		return cookie;
	}
	
	public boolean hasExpired() {
		return (System.currentTimeMillis() - lastUpdated) > (cookie.getMaxAge() * 1000);
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof CookieHolder) {
			CookieHolder obj2 = (CookieHolder) obj;
			return ((CookieHolder) obj).getCookie().getName().equals(obj2.getCookie().getName());
		}
		return false;
	}
	
	public int hashCode() {
		return cookie.getName().hashCode();
	}
}
