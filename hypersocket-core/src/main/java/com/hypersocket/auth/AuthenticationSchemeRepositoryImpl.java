/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.annotation.HypersocketExtension;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.repository.SystemRestriction;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;

@Repository
public class AuthenticationSchemeRepositoryImpl extends AbstractResourceRepositoryImpl<AuthenticationScheme>
		implements AuthenticationSchemeRepository {

	static Logger log = LoggerFactory.getLogger(AuthenticationSchemeRepositoryImpl.class);
	
	@Autowired
	private AuthenticationModuleRepository moduleRepository; 
	
	private Map<String,AuthenticationSchemeRegistration> schemes = new HashMap<>();
	
	@Override
	public AuthenticationSchemeRegistration getRegistration(String scheme) {
		return schemes.get(scheme);
	}
	
	@Override
	@Transactional
	public void deleteRealm(Realm realm) {
		
		moduleRepository.deleteRealm(realm);
		int count = 0;
		for(AuthenticationScheme s : allEntities(AuthenticationScheme.class, new RealmRestriction(realm))) {
			s.getAllowedRoles().clear();
			s.getDeniedRoles().clear();
			save(s);
			delete(s);
			count++;
		}
		
		log.info(String.format("Deleted %d AuthenticationScheme", count));
	}
	
	CriteriaConfiguration ORDER_BY_PRIORITY = new CriteriaConfiguration() {
		@Override
		public void configure(Criteria criteria) {
			criteria.setFetchMode("modules", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			criteria.addOrder(Order.asc("priority"));
		}
	};

	@Override
	@Transactional(readOnly=true)
	public Set<Role> getAllowedRoles(AuthenticationScheme scheme) {
		return new LinkedHashSet<>(getSchemeById(scheme.getId()).getAllowedRoles());
	}

	@Override
	@Transactional(readOnly=true)
	public Set<Role> getDeniedRoles(AuthenticationScheme scheme) {
		return new LinkedHashSet<>(getSchemeById(scheme.getId()).getDeniedRoles());
	}

	@Override
	@Transactional
	public AuthenticationScheme createScheme(Realm realm, String name,
			List<String> templates, String resourceKey, boolean hidden,
			Integer maximumModules, AuthenticationModuleType type, boolean supportsHomeRedirect) {
		return createScheme(realm, name, templates, resourceKey, hidden, maximumModules, type, null, null, supportsHomeRedirect);
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
			String lastButtonResourceKey,
			boolean supportsHomeRedirect) {
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
		scheme.setSupportsHomeRedirect(supportsHomeRedirect);
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
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> allSchemes(Realm realm) {
		return allEntities(AuthenticationScheme.class, ORDER_BY_PRIORITY, 
				new HiddenCriteria(false), 
				new RealmRestriction(realm));
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> allEnabledSchemes(Realm realm) {
		return allEntities(AuthenticationScheme.class, ORDER_BY_PRIORITY, 
				new HiddenCriteria(false), 
				new RealmRestriction(realm),
				new EnabledSchemesCriteria(realm));
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
		return get("resourceKey", resourceKey, AuthenticationScheme.class, new RealmRestriction(realm), new EnabledSchemesCriteria(realm));
	}
	
	@Override
	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeByResourceKey2(Realm realm, String resourceKey) {
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

//	@Override
//	@Transactional(readOnly=true)
//	public List<AuthenticationScheme> getAuthenticationSchemes(Realm realm, 
//			final boolean enabledOnly) {
//		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
//				false), new HiddenCriteria(false), new DistinctRootEntity(), 
//				new RealmRestriction(realm), new EnabledSchemesCriteria(realm));
//	}
	
	class EnabledSchemesCriteria implements CriteriaConfiguration {
		
		Realm realm;
		
		EnabledSchemesCriteria(Realm realm) {
			this.realm = realm;
		}
		
		@Override
		public void configure(Criteria criteria) {
			criteria.add(Restrictions.or(Restrictions.eq("system", false),
					Restrictions.and(Restrictions.in("resourceKey", getEnabledSchemes(realm)), 
							Restrictions.eq("system", true))));
		}
	}
	
	private Collection<String> getEnabledSchemes(Realm realm) {
		List<String> results = new ArrayList<>();
		for(AuthenticationSchemeRegistration r : schemes.values()) {
			if(r.isEnabled()) {
				results.add(r.getResourceKey());
			}
		}
		return results;
	}
	
//	@Override
//	@Transactional(readOnly=true)
//	public List<AuthenticationScheme> getAuthenticationSchemes(Realm realm) {
//		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
//				false), new HiddenCriteria(false), new DistinctRootEntity(), 
//				new RealmRestriction(realm), new EnabledSchemesCriteria(realm));
//	}

	@Override
	@Transactional(readOnly=true)
	public AuthenticationScheme getSchemeById(Long id) {
		return get("id", id, AuthenticationScheme.class);
	}

	@Override
	@Transactional
	public void saveScheme(AuthenticationScheme s) {
		s.setResourceCategory("authenticationScheme");
		save(s);
	}

	@Override
	protected Class<AuthenticationScheme> getResourceClass() {
		return AuthenticationScheme.class;
	}

	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> getCustomAuthenticationSchemes(Realm realm) {
		return allEntities(AuthenticationScheme.class, new DeletedCriteria(
				false), new HiddenCriteria(false), new DistinctRootEntity(), 
				new RealmRestriction(realm), new SystemRestriction(false));
	}

	@Override
	public void registerAuthenticationScheme(String scheme) {
		schemes.put(scheme, new DefaultRegistration(scheme));
	}
	
	@Override
	public void registerAuthenticationScheme(AuthenticationSchemeRegistration scheme) {
		schemes.put(scheme.getResourceKey(), scheme);
	}
	
	class DefaultRegistration implements AuthenticationSchemeRegistration {

		String resourceKey;
		
		DefaultRegistration(String resourceKey) {
			this.resourceKey = resourceKey;
		}
		
		@Override
		public String getResourceKey() {
			return resourceKey;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
		
	}

	@Override
	public boolean isEnabled(String template) {
		for(AuthenticationSchemeRegistration r : schemes.values()) {
			if(r.getResourceKey().equals(template)) {
				return r.isEnabled();
			}
		}
		return false;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<AuthenticationScheme> get2faSchemes(Realm realm) {
		return list("scheme2fa", Boolean.TRUE, AuthenticationScheme.class, new RealmCriteria(realm));
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<AuthenticationScheme> getSystemSchemes(Realm realm) {
		return list("system", Boolean.TRUE, AuthenticationScheme.class, new RealmCriteria(realm));
	}
	
	@Override
	@Transactional(readOnly=true)
	public AuthenticationScheme get2faScheme(Realm realm, String authenticator) {
		return get("authenticator2fa", authenticator, AuthenticationScheme.class, new RealmCriteria(realm));
	}
}
