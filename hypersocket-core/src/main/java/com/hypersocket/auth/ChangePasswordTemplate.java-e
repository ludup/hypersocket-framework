/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import com.hypersocket.i18n.I18N;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.input.ParagraphField;
import com.hypersocket.input.PasswordInputField;

public class ChangePasswordTemplate extends FormTemplate {

	public static final String PASSWORD_FIELD = "password";
	public static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

	public ChangePasswordTemplate(AuthenticationState state) {

		setResourceKey(UsernameAndPasswordAuthenticator.RESOURCE_KEY);

		fields.add(new ParagraphField("info.passwordChangeRequired", true));
		fields.add(new PasswordInputField(PASSWORD_FIELD, "", true, I18N
				.getResource(state.getLocale(),
						AuthenticationServiceImpl.RESOURCE_BUNDLE,
						"password.label")));
		fields.add(new PasswordInputField(CONFIRM_PASSWORD_FIELD, "", true,
				I18N
				.getResource(state.getLocale(),
						AuthenticationServiceImpl.RESOURCE_BUNDLE,
						"confirmPassword.label")));
	}
}
