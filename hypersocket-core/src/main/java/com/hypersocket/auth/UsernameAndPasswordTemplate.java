/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.i18n.I18N;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.input.Option;
import com.hypersocket.input.PasswordInputField;
import com.hypersocket.input.SelectInputField;
import com.hypersocket.input.TextInputField;
import com.hypersocket.realm.Realm;

@XmlRootElement(name = "form")
public class UsernameAndPasswordTemplate extends FormTemplate {

	public static final String CANNOT_CHANGE_USERNAME = "cannotChangeUsername";
	
	public static final String USERNAME_FIELD = "username";
	public static final String PASSWORD_FIELD = "password";

	@SuppressWarnings("rawtypes")
	public UsernameAndPasswordTemplate(AuthenticationState state, Map params, List<Realm> realms, Realm defaultRealm) {
		super(state.getInitialSchemeResourceKey());
		setResourceKey(UsernameAndPasswordAuthenticator.RESOURCE_KEY);

		if(realms.size() > 1) {
			SelectInputField select = new SelectInputField("realm", defaultRealm.getName(), true, "realm.label");
			select.setOnChange("changeLogonRealm");
			for(Realm realm : realms) {
				select.addOption(new Option(realm.getName(), realm.getName(), realm.equals(state.getRealm()), false));
			}
			fields.add(select);
		}
		
		if(Objects.isNull(state.getPrincipal())) {
		
			TextInputField username = new TextInputField(USERNAME_FIELD, "", true, I18N
					.getResource(state.getLocale(),
							AuthenticationServiceImpl.RESOURCE_BUNDLE,
							"username.label"));

			fields.add(username);
		} else {
			TextInputField username = new TextInputField(USERNAME_FIELD, 
					state.getPrincipal().getPrincipalName(), true, I18N
					.getResource(state.getLocale(),
							AuthenticationServiceImpl.RESOURCE_BUNDLE,
							"username.label"));

			if(state.hasEnvironmentVariable(CANNOT_CHANGE_USERNAME) && 
					Boolean.TRUE.equals(state.getEnvironmentVariable(CANNOT_CHANGE_USERNAME))) {
				username.setReadOnly(true);
			}
			
			fields.add(username);
		}
		
		fields.add(new PasswordInputField(PASSWORD_FIELD, "", true, "Password"));

	}

}
