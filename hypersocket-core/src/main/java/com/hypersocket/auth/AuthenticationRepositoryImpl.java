/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DetachedCriteriaConfiguration;
import com.hypersocket.repository.DistinctRootEntity;

@Repository
public class AuthenticationRepositoryImpl extends AbstractRepositoryImpl<Long>
		implements AuthenticationRepository {

	DetachedCriteriaConfiguration ORDER_BY_PRIORITY = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("modules", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			criteria.addOrder(Order.asc("priority"));
		}
	};

	DetachedCriteriaConfiguration JOIN_MODULES_AND_CONSTRAINTS = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("modules", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	DetachedCriteriaConfiguration JOIN_TEMPLATES = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("template", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	DetachedCriteriaConfiguration JOIN_CREDENTIALS = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("credentials", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		}
	};

	public Credential createCredential(CredentialType type, Integer index,
			String resourceKey) {
		Credential cred = new Credential();
		cred.setType(type);
		cred.setIndex(index);
		cred.setResourceKey(resourceKey);
		save(cred);
		return cred;
	}

	@Override
	public AuthenticationScheme createScheme(String name,
			List<String> templates, String resourceKey, boolean hidden) {

		AuthenticationScheme scheme = new AuthenticationScheme();
		scheme.setName(name);
		scheme.setResourceKey(resourceKey);
		scheme.setHidden(hidden);
		save(scheme);

		int idx = 0;
		for (String t : templates) {
			AuthenticationModule m = new AuthenticationModule();
			m.setScheme(scheme);
			m.setTemplate(t);
			m.setIndex(idx++);
			save(m);
		}

		return scheme;
	}

	@Override
	public List<AuthenticationScheme> allSchemes() {
		return allEntities(AuthenticationScheme.class, ORDER_BY_PRIORITY);
	}

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

	@Override
	public AuthenticationScheme createScheme(String name, List<String> modules,
			String resourceKey) {
		return createScheme(name, modules, resourceKey, false);
	}

	public AuthenticationScheme getScheme(String name) {
		return get("name", name, AuthenticationScheme.class);
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
	public AuthenticationScheme getSchemeById(Long id) {
		return get("id", id, AuthenticationScheme.class);
	}

	@Override
	public void updateSchemeModules(List<AuthenticationModule> moduleList) {
		for (AuthenticationModule module : moduleList) {
			save(module);
		}
	}

	public AuthenticationModule createAuthenticationModule(
			AuthenticationModule authenticationModule) {
		save(authenticationModule);

		return authenticationModule;
	}

	public AuthenticationModule updateAuthenticationModule(
			AuthenticationModule authenticationModule) {
		save(authenticationModule);
		return getModuleById(authenticationModule.getId());
	}

	public void deleteModule(AuthenticationModule authenticationModule) {
		authenticationModule.setDeleted(true);
		save(authenticationModule);
	}

}
