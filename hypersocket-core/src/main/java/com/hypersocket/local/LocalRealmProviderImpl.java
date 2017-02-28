/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;

@Repository
public class LocalRealmProviderImpl extends AbstractLocalRealmProviderImpl implements LocalRealmProvider {

	static final String RESOURCE_BUNDLE = "LocalRealm";

	public final static String REALM_RESOURCE_CATEGORY = "local";

	public final static String USER_RESOURCE_CATEGORY = "localUser";
	public final static String GROUP_RESOURCE_CATEGORY = "localGroup";

	public final static String FIELD_FULLNAME = "fullname";
	public final static String FIELD_EMAIL = "email";
	public final static String FIELD_MOBILE = "mobile";
	public final static String FIELD_PASSWORD_ENCODING = "password.encoding";

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	RealmService realmService;

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	PasswordEncryptionService encryptionService;
	
	@Autowired
	I18NService i18nService;
	
	PropertyCategory userDetailsCategory;

	Set<String> defaultProperties = new HashSet<String>();
	
	@PostConstruct
	private void registerProvider() throws Exception {
		i18nService.registerBundle(LocalRealmProviderImpl.RESOURCE_BUNDLE);

		defaultProperties.add("fullname");
		defaultProperties.add("email");
		defaultProperties.add("mobile");
		
		realmService.registerRealmProvider(this);

		loadPropertyTemplates("localRealmTemplate.xml");

		userRepository.loadPropertyTemplates("localUserTemplate.xml");
		userRepository.registerPropertyResolver(userAttributeService.getPropertyResolver());
	}

	@Override
	public String getModule() {
		return REALM_RESOURCE_CATEGORY;
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public void assertCreateRealm(Map<String, String> properties) throws ResourceException {
		
	}

}
