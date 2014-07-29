/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DetachedCriteriaConfiguration;
import com.hypersocket.repository.DistinctRootEntity;

@Repository
public class AuthenticationModuleRepositoryImpl extends AbstractEntityRepositoryImpl<AuthenticationModule,Long>
		implements AuthenticationModuleRepository {

	
	@Override
	public List<AuthenticationModule> getModulesForScheme(
			AuthenticationScheme scheme) {
		return allEntities(AuthenticationModule.class, new SchemeRestriction(
				scheme));
	}

	class SchemeRestriction implements DetachedCriteriaConfiguration {

		AuthenticationScheme scheme;

		SchemeRestriction(AuthenticationScheme scheme) {
			this.scheme = scheme;
		}

		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.add(Restrictions.eq("scheme", scheme));

		}
	}

	public AuthenticationScheme getSchemeByResourceKey(String resourceKey) {
		return get("resourceKey", resourceKey, AuthenticationScheme.class);
	}

	public List<AuthenticationScheme> getAuthenticationSchemes() {

		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
				false), new DistinctRootEntity());

	}

	public List<AuthenticationModule> getAuthenticationModules() {
		return allEntities(AuthenticationModule.class, new DeletedCriteria(
				false), new DistinctRootEntity());
	}

	public List<AuthenticationModule> getAuthenticationModulesByScheme(
			final AuthenticationScheme authenticationScheme) {
		return allEntities(AuthenticationModule.class, new DeletedCriteria(
				false), new DistinctRootEntity(),
				new DetachedCriteriaConfiguration() {

					@Override
					public void configure(DetachedCriteria criteria) {
						criteria.add(Restrictions.eq("scheme",
								authenticationScheme));
						criteria.addOrder(Order.asc("idx"));
					}
				});
	}

	@Override
	public AuthenticationModule getModuleById(Long id) {
		return get("id", id, AuthenticationModule.class);
	}

	@Override
	public void updateSchemeModules(List<AuthenticationModule> moduleList) {
		for (AuthenticationModule module : moduleList) {
			saveEntity(module);
		}
	}

	public AuthenticationModule createAuthenticationModule(
			AuthenticationModule authenticationModule) {
		saveEntity(authenticationModule);

		return authenticationModule;
	}

	public AuthenticationModule updateAuthenticationModule(
			AuthenticationModule authenticationModule) {
		saveEntity(authenticationModule);
		return getModuleById(authenticationModule.getId());
	}

	public void deleteModule(AuthenticationModule authenticationModule) {
		authenticationModule.setDeleted(true);
		saveEntity(authenticationModule);
	}

	@Override
	protected Class<AuthenticationModule> getEntityClass() {
		return AuthenticationModule.class;
	}
}
