/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;

@Repository
public class AuthenticationModuleRepositoryImpl extends AbstractEntityRepositoryImpl<AuthenticationModule,Long>
		implements AuthenticationModuleRepository {

	
	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationModule> getModulesForScheme(
			AuthenticationScheme scheme) {
		return allEntities(AuthenticationModule.class, new SchemeRestriction(
				scheme));
	}

	class SchemeRestriction implements CriteriaConfiguration {

		AuthenticationScheme scheme;

		SchemeRestriction(AuthenticationScheme scheme) {
			this.scheme = scheme;
		}

		@Override
		public void configure(Criteria criteria) {
			criteria.add(Restrictions.eq("scheme", scheme));

		}
	}

	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeByResourceKey(String resourceKey) {
		return get("resourceKey", resourceKey, AuthenticationScheme.class);
	}

	@Transactional(readOnly=true)
	public List<AuthenticationScheme> getAuthenticationSchemes() {

		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
				false), new DistinctRootEntity());

	}

	@Transactional(readOnly=true)
	public List<AuthenticationModule> getAuthenticationModules() {
		return allEntities(AuthenticationModule.class, new DeletedCriteria(
				false), new DistinctRootEntity());
	}

	@Transactional(readOnly=true)
	public List<AuthenticationModule> getAuthenticationModulesByScheme(
			final AuthenticationScheme authenticationScheme) {
		return allEntities(AuthenticationModule.class, new DeletedCriteria(
				false), new DistinctRootEntity(),
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("scheme",
								authenticationScheme));
						criteria.addOrder(Order.asc("idx"));
					}
				});
	}

	@Override
	@Transactional(readOnly=true)
	public AuthenticationModule getModuleById(Long id) {
		return get("id", id, AuthenticationModule.class);
	}

	@Override
	@Transactional
	public void updateSchemeModules(List<AuthenticationModule> moduleList) {
		for (AuthenticationModule module : moduleList) {
			saveEntity(module);
		}
	}

	@Transactional
	public AuthenticationModule createAuthenticationModule(
			AuthenticationModule authenticationModule) {
		saveEntity(authenticationModule);

		return authenticationModule;
	}

	@Transactional
	public AuthenticationModule updateAuthenticationModule(
			AuthenticationModule authenticationModule) {
		saveEntity(authenticationModule);
		return getModuleById(authenticationModule.getId());
	}

	@Transactional
	public void deleteModule(AuthenticationModule authenticationModule) {
		authenticationModule.setDeleted(true);
		saveEntity(authenticationModule);
	}

	@Override
	protected Class<AuthenticationModule> getEntityClass() {
		return AuthenticationModule.class;
	}
}
