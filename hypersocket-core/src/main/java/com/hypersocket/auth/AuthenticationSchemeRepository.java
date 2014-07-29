/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;

import com.hypersocket.repository.AbstractEntityRepository;

public interface AuthenticationSchemeRepository extends AbstractEntityRepository<AuthenticationScheme,Long> {

	public AuthenticationScheme createScheme(String name, List<String> modules,
			String resourceKey);

	public AuthenticationScheme createScheme(String name, List<String> modules,
			String resourceKey, boolean hidden);

	List<AuthenticationScheme> allSchemes();

	public AuthenticationScheme getSchemeByResourceKey(String resourceKey);

	public List<AuthenticationScheme> getAuthenticationSchemes();

	public AuthenticationScheme getSchemeById(Long id);

	public void saveScheme(AuthenticationScheme s);

	public AuthenticationScheme getSchemeByName(String name);
}
