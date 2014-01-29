/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import com.hypersocket.HypersocketVersion;


public abstract class AuthenticationResult {

	String bannerMsg;
	String errorMsg;
	boolean showLocales;
	String version = HypersocketVersion.getVersion();
	
	public AuthenticationResult() {	
	}
	
	public AuthenticationResult(String bannerMsg, String errorMsg, boolean showLocales) {
		this.bannerMsg = bannerMsg;
		this.errorMsg = errorMsg;
		this.showLocales = showLocales;
	}

	public String getBannerMsg() {
		return bannerMsg;
	}

	public void setBannerMsg(String info) {
		this.bannerMsg = info;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getVersion() {
		return version;
	}	
	
	public boolean isShowLocales() {
		return showLocales;
	}

	public void setShowLocales(boolean showLocales) {
		this.showLocales = showLocales;
	}
	
}
