/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.i18n.I18N;
import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.json.input.PasswordInputField;

@XmlRootElement(name = "form")
public class PasswordTemplate extends FormTemplate {

	public static final String PASSWORD_FIELD = "password";

	@SuppressWarnings("rawtypes")
	public PasswordTemplate(
			AuthenticationState state, Map params) {
		super(state.getInitialSchemeResourceKey());
		fields.add(new PasswordInputField(PASSWORD_FIELD, "", true, I18N.getResource(state.getLocale(),
				AuthenticationServiceImpl.RESOURCE_BUNDLE, "password.label")));

	}

}
