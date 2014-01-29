/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.input.FormTemplate;
import com.hypersocket.input.HiddenInputField;
import com.hypersocket.input.Option;
import com.hypersocket.input.PasswordInputField;
import com.hypersocket.input.SelectInputField;
import com.hypersocket.input.TextInputField;
import com.hypersocket.realm.Realm;

@XmlRootElement(name = "form")
public class UsernameAndPasswordTemplate extends FormTemplate {

	public static final String REALM_FIELD = "realm";
	public static final String USERNAME_FIELD = "username";
	public static final String PASSWORD_FIELD = "password";

	@SuppressWarnings("rawtypes")
	public UsernameAndPasswordTemplate(boolean showRealm,
			boolean requiresRealm, List<Realm> realms,
			AuthenticationState state, Map params) {

		setResourceKey(UsernameAndPasswordAuthenticator.RESOURCE_KEY);
		if (showRealm) {
			if (realms.size() > 1) {
				SelectInputField select = new SelectInputField(REALM_FIELD,
						 "", requiresRealm);
				select.addOption(new Option("select.label", "", false, true));
				for (Realm r : realms) {
					select.addOption(new Option(r.getName(), r.getName(), r
							.equals(state.getRealm())
							|| r.getName().equals(AuthenticationUtils.getRequestParameter(
									params, UsernameAndPasswordTemplate.REALM_FIELD)), false));
				}
				fields.add(select);
			} else {
				fields.add(new HiddenInputField(REALM_FIELD, realms.get(0)
						.getName()));
			}
		}

		TextInputField username = new TextInputField(USERNAME_FIELD, "", true);

		if (state.getPrincipal() != null) {
			username.setDefaultValue(state.getPrincipal().getName());
		} else if (params.containsKey(USERNAME_FIELD)) {
			username.setDefaultValue(AuthenticationUtils.getRequestParameter(
					params, UsernameAndPasswordTemplate.USERNAME_FIELD));
		}

		fields.add(username);
		fields.add(new PasswordInputField(PASSWORD_FIELD, "", true));

	}

}
