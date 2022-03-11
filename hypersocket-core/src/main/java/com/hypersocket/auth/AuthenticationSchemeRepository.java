/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.Collection;
import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface AuthenticationSchemeRepository extends AbstractResourceRepository<AuthenticationScheme> {

	AuthenticationScheme createScheme(Realm realm, String name, List<String> modules,
			String resourceKey, boolean hidden, Integer maximum, AuthenticationModuleType type, boolean supportsHomeRedirect);

	List<AuthenticationScheme> allSchemes(Realm realm);

	List<AuthenticationScheme> allEnabledSchemes(Realm realm);
	
	AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey);

	List<AuthenticationScheme> getCustomAuthenticationSchemes(Realm realm);

	AuthenticationScheme getSchemeById(Long id);

	void saveScheme(AuthenticationScheme s);

	Long getSchemeByResourceKeyCount(Realm realm, String resourceKey);

	AuthenticationScheme createScheme(Realm realm, String name, List<String> templates, String resourceKey,
			boolean hidden, Integer maximumModules, AuthenticationModuleType type, String allowedModules,
			String lastButtonResourceKey, boolean supportsHomeRedirect);

	void registerAuthenticationScheme(AuthenticationSchemeRegistration scheme);

	AuthenticationScheme getSchemeByResourceKey2(Realm realm, String resourceKey);

	void registerAuthenticationScheme(String scheme);

	boolean isEnabled(String template);

	Collection<AuthenticationScheme> get2faSchemes(Realm realm);

	AuthenticationScheme get2faScheme(Realm realm, String authenticator);

	List<AuthenticationScheme> getSystemSchemes(Realm currentRealm);

	AuthenticationSchemeRegistration getRegistration(String scheme);

	
}
