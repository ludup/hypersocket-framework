/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

public class CredentialsUpdate {

	private Long principalId;
	private String password;
	private boolean forceChange;
	private boolean resendNewUserNotification;
	
	public CredentialsUpdate() {
		
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isForceChange() {
		return forceChange;
	}
	
	public void setForceChange(boolean forceChange) {
		this.forceChange = forceChange;
	}
	
	public Long getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}

	public boolean isResendNewUserNotification() {
		return resendNewUserNotification;
	}

	public void setResendNewUserNotification(boolean resendNewUserNotification) {
		this.resendNewUserNotification = resendNewUserNotification;
	}
	
}
