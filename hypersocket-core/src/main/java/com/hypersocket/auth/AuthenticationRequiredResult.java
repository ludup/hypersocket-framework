/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.input.FormTemplate;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.realm.Realm;

public class AuthenticationRequiredResult extends AuthenticationResult {

	private FormTemplate formTemplate;
	private boolean lastErrorIsResourceKey;
	private boolean isNew;
	private boolean isFirst;
	private boolean isLast;
	private boolean lastResultSuccessful;
	private boolean inPostAuthentication;
	private String lastButtonResourceKey;
	private Realm realm;
	private Map<String, String[]> requestParameters;
	private int page;
	
	public AuthenticationRequiredResult() {

	}

	public AuthenticationRequiredResult(String bannerMsg, String errorMsg, String errorStyle,
			boolean lastErrorIsResourceKey, FormTemplate formTemplate,
			boolean showLocales, boolean isNew, boolean isFirst, boolean isLast,
			boolean lastResultSuccessful, boolean inPostAuthentication,
			String lastButtonResourceKey, Realm realm, int page) {
		this(bannerMsg, errorMsg, errorStyle, lastErrorIsResourceKey, formTemplate, showLocales, isNew, isFirst, isLast, lastResultSuccessful, inPostAuthentication, lastButtonResourceKey, realm, page, null);
	}

	public AuthenticationRequiredResult(String bannerMsg, String errorMsg, String errorStyle,
			boolean lastErrorIsResourceKey, FormTemplate formTemplate,
			boolean showLocales, boolean isNew, boolean isFirst, boolean isLast,
			boolean lastResultSuccessful, boolean inPostAuthentication,
			String lastButtonResourceKey, Realm realm, int page, Map<String, String[]> requestParameters) {
		super(bannerMsg, errorMsg, errorStyle, showLocales);
		this.formTemplate = formTemplate;
		this.lastErrorIsResourceKey = lastErrorIsResourceKey;
		this.isNew = isNew;
		this.isFirst = isFirst;
		this.isLast = isLast;
		this.lastResultSuccessful = lastResultSuccessful;
		this.inPostAuthentication = inPostAuthentication;
		this.lastButtonResourceKey = lastButtonResourceKey;
		this.realm = realm;
		this.requestParameters = requestParameters;
		this.page = page;
	}

	public int getNonce() {
		return page;
	}
	
	public Map<String, String[]> getRequestParameters() {
		return requestParameters;
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	public void setFormTemplate(FormTemplate template) {
		this.formTemplate = template;
	}

	public boolean getLastErrorIsResourceKey() {
		return lastErrorIsResourceKey;
	}

	public boolean isLastResultSuccessfull() {
		return lastResultSuccessful;
	}

	public boolean isNewSession() {
		return isNew;
	}

	public boolean isFirst() {
		return isFirst;
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public boolean isPostAuthentication() {
		return inPostAuthentication;
	}
	
	public String getLastButtonResourceKey() {
		return lastButtonResourceKey;
	}

	public Realm getRealm() {
		return realm;
	}

	@Override
	public String toString() {
		return "AuthenticationRequiredResult [formTemplate=" + formTemplate
				+ ", lastErrorIsResourceKey=" + lastErrorIsResourceKey
				+ ", isNew=" + isNew + ", isLast=" + isLast
				+ ", lastResultSuccessful=" + lastResultSuccessful
				+ ", bannerMsg=" + bannerMsg + ", errorMsg=" + errorMsg
				+ ", showLocales=" + showLocales + ", success=" + success
				+ ", version=" + version + ", principal=" + principal + "]";
	}

}
