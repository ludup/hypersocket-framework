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

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.OrderByAsc;

@Repository
public class AuthenticationModuleRepositoryImpl extends AbstractEntityRepositoryImpl<AuthenticationModule,Long>
		implements AuthenticationModuleRepository {

	static Logger log = LoggerFactory.getLogger(AuthenticationModuleRepositoryImpl.class);
	
	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationModule> getModulesForScheme(
			AuthenticationScheme scheme) {
		return allEntities(AuthenticationModule.class, new SchemeRestriction(
				scheme));
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<AuthenticationModule> getModulesForRealm(
			final Realm realm) {
		return list(AuthenticationModule.class, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.createAlias("scheme","s");
				criteria.add(Restrictions.eq("s.realm", realm));
			}
		});
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
	@Override
	public boolean isAuthenticatorInUse(final Realm realm, String resourceKey, String... schemes) {
		return getCount(AuthenticationModule.class, "template", resourceKey, new DeletedCriteria(false), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.createAlias("scheme", "s");
				criteria.add(Restrictions.eq("s.realm", realm));
				criteria.add(Restrictions.eq("s.deleted", false));
				if(schemes.length > 0) {
					criteria.add(Restrictions.in("s.resourceKey", schemes));
				}
			}
			
		}) > 0;
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
				false), new DistinctRootEntity(), new OrderByAsc("idx"));
	}

	@Transactional(readOnly=true)
	public List<AuthenticationModule> getAuthenticationModulesByScheme(
			final AuthenticationScheme authenticationScheme) {
		return allEntities(AuthenticationModule.class, new DeletedCriteria(
				false), new DistinctRootEntity(),
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.eq("scheme", authenticationScheme));
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

	@Override
	@Transactional(readOnly=true)
	public Collection<AuthenticationScheme> getSchemesForModule(Realm realm, final String... resourceKeys) {
		return list(AuthenticationScheme.class, new DeletedCriteria(false), new RealmRestriction(realm), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.createAlias("modules", "m");
				criteria.add(Restrictions.eq("m.deleted", false));
				criteria.add(Restrictions.in("m.template", resourceKeys));
			}
			
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean isModuleInScheme(Realm realm, final String resourceKey, final String... resourceKeys) {
		return getCount(AuthenticationScheme.class, new DeletedCriteria(false), new RealmRestriction(realm), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("resourceKey", resourceKey));
				criteria.createAlias("modules", "m");
				criteria.add(Restrictions.in("m.template", resourceKeys));
			}
			
		}) > 0; 
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		
		int count = 0;
		for(AuthenticationModule m : getModulesForRealm(realm)) {
			delete(m);
			count++;
		}
		log.info(String.format("Deleted %d AuthenticationModule", count));
	}
}
