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
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.annotation.HypersocketExtension;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;

@Repository
public class AuthenticationSchemeRepositoryImpl extends AbstractEntityRepositoryImpl<AuthenticationScheme,Long>
		implements AuthenticationSchemeRepository {

	CriteriaConfiguration ORDER_BY_PRIORITY = new CriteriaConfiguration() {
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("modules", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			criteria.addOrder(Order.asc("priority"));
		}
	};

	@Override
	@Transactional
	public AuthenticationScheme createScheme(Realm realm, String name,
			List<String> templates, String resourceKey, boolean hidden,
			Integer maximumModules, AuthenticationModuleType type) {
		return createScheme(realm, name, templates, resourceKey, hidden, maximumModules, type, null, null);
	}
	
	@Override
	@Transactional
	public AuthenticationScheme createScheme(Realm realm, 
			String name,
			List<String> templates, 
			String resourceKey, 
			boolean hidden,
			Integer maximumModules, 
			AuthenticationModuleType type, 
			String allowedModules, 
			String lastButtonResourceKey) {
		AuthenticationScheme scheme = new AuthenticationScheme();
		scheme.setName(name);
		scheme.setRealm(realm);
		scheme.setResourceKey(resourceKey);
		scheme.setResourceCategory("authenticationScheme");
		scheme.setHidden(hidden);
		scheme.setType(type);
		scheme.setMaximumModules(maximumModules);
		scheme.setAllowedModules(allowedModules);
		scheme.setLastButtonResourceKey(lastButtonResourceKey);
		
		saveEntity(scheme);

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
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> allSchemes(Realm realm) {
		return allEntities(AuthenticationScheme.class, ORDER_BY_PRIORITY, new HiddenCriteria(false), new RealmRestriction(realm));
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

	@Override
	@HypersocketExtension
	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeByResourceKey(Realm realm, String resourceKey) {
		return get("resourceKey", resourceKey, AuthenticationScheme.class, new RealmRestriction(realm));
	}
	
	@Override
	@Transactional(readOnly=true)
	public Long getSchemeByResourceKeyCount(final Realm realm, String resourceKey) {
		return getCount(AuthenticationScheme.class, "resourceKey", resourceKey, new CriteriaConfiguration() {
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("realm", realm));
				
			}
		});
	}

	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> getAuthenticationSchemes(Realm realm) {
		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
				false), new HiddenCriteria(false), new DistinctRootEntity(), new RealmRestriction(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeById(Long id) {
		return get("id", id, AuthenticationScheme.class);
	}

	@Override
	@Transactional
	public void saveScheme(AuthenticationScheme s) {
		s.setResourceCategory("authenticationScheme");
		saveEntity(s);
	}

	@Override
	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeByName(Realm realm, String name) {
		return get("name", name, AuthenticationScheme.class, new RealmRestriction(realm));
	}

	@Override
	protected Class<AuthenticationScheme> getEntityClass() {
		return AuthenticationScheme.class;
	}
}
