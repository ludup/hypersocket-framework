/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface AuthenticationSchemeRepository extends AbstractResourceRepository<AuthenticationScheme> {

	public AuthenticationScheme createScheme(Realm realm, String name, List<String> modules,
			String resourceKey, boolean hidden, Integer maximum, AuthenticationModuleType type, boolean supportsHomeRedirect);

	List<AuthenticationScheme> allSchemes(Realm realm);

	public AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey);

	public List<AuthenticationScheme> getAuthenticationSchemes(Realm realm, boolean onlyEnabled);
	
	public List<AuthenticationScheme> getCustomAuthenticationSchemes(Realm realm);

	public List<AuthenticationScheme> getAuthenticationSchemes(Realm realm);

	public AuthenticationScheme getSchemeById(Long id);

	public void saveScheme(AuthenticationScheme s);

	public AuthenticationScheme getSchemeByName(Realm realm, String name);

	Long getSchemeByResourceKeyCount(Realm realm, String resourceKey);

	AuthenticationScheme createScheme(Realm realm, String name, List<String> templates, String resourceKey,
			boolean hidden, Integer maximumModules, AuthenticationModuleType type, String allowedModules,
			String lastButtonResourceKey, boolean supportsHomeRedirect);

	void enableAuthenticationScheme(String scheme);
}
