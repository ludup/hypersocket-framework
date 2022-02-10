/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.SessionService;

@Repository
public class LocalRealmProviderImpl extends AbstractLocalRealmProviderImpl implements LocalRealmProvider {

	static Logger log = LoggerFactory.getLogger(LocalRealmProviderImpl.class);

	static final String RESOURCE_BUNDLE = "LocalRealm";

	public final static String REALM_RESOURCE_CATEGORY = "local";

	public final static String USER_RESOURCE_CATEGORY = "localUser";
	public final static String GROUP_RESOURCE_CATEGORY = "localGroup";

	public final static String FIELD_FULLNAME = "description";
	public final static String FIELD_EMAIL = "email";
	public final static String FIELD_MOBILE = "mobile";
	public final static String FIELD_PASSWORD_ENCODING = "password.encoding";


	@Autowired
	private LocalGroupRepository groupRepository;

	@Autowired
	private RealmService realmService;

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private I18NService i18nService;
	
	@Autowired
	private LocalPrincipalTemplateRepository templateRepository;
	
	@Autowired
	private SessionService sessionService; 
	
	@PostConstruct
	private void registerProvider() throws Exception {
		i18nService.registerBundle(LocalRealmProviderImpl.RESOURCE_BUNDLE);

		defaultProperties.add("description");

		if(Boolean.getBoolean("logonbox.forceAdminPassword")) {
			sessionService.executeInSystemContext(()->{
				try {
				
					Principal principal = realmService.getPrincipalByName(realmService.getSystemRealm(), System.getProperty("logonbox.existingAdminUsername", "admin"), PrincipalType.USER);
					if(principal!=null) {
						
							realmService.updateUser(principal.getRealm(), principal, 
									System.getProperty("logonbox.newAdminUsername", principal.getPrincipalName()), 
									new HashMap<>(), Collections.emptyList());
							realmService.setPassword(principal, System.getProperty("logonbox.newAdminPassword", "Qwerty123?"), 
									Boolean.getBoolean("logonbox.forceTempPassword"),
									true);
	
					} else if(Boolean.getBoolean("logonbox.createAdmin")) {
						principal = realmService.createUser(realmService.getSystemRealm(),
								System.getProperty("logonbox.newAdminUsername", "admin"),
								new HashMap<>(),
								Collections.emptyList(),
								System.getProperty("logonbox.newAdminPassword", "Qwerty123?"),
								Boolean.getBoolean("logonbox.forceTempPassword"),
								false, false);
						
						permissionService.assignRole(permissionService.getSystemAdministratorRole(), principal);
								
					}

				} catch (ResourceException | AccessDeniedException e) {
					log.error("Failed to reset admin password");
				}
			});
		}
		
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

	@Override
	public void setUserProperty(Principal principal, String resourceKey, Long val) {
		userRepository.setValue(principal, resourceKey, val);
	}

	@Override
	public void setUserProperty(Principal principal, String resourceKey, Integer val) {
		userRepository.setValue(principal, resourceKey, val);
	}

	@Override
	public void setUserProperty(Principal principal, String resourceKey, Boolean val) {
		userRepository.setValue(principal, resourceKey, val);
	}

	@Override
	public void setUserProperty(Principal principal, String resourceKey, String val) {
		userRepository.setValue(principal, resourceKey, val);
	}

	@Override
	public Principal reconcileUser(Principal principal) throws ResourceException {
		return principal;
	}

	@Override
	public void resetRealm(Realm realm) throws ResourceNotFoundException, AccessDeniedException {
		
		List<Role> roles = new ArrayList<Role>();
		roles.add(permissionService.getRealmAdministratorRole(realm));
		if(realm.isSystem()) {
			roles.add(permissionService.getSystemAdministratorRole());
		}
		
		userRepository.resetRealm(permissionService.iteratePrincipalsByRole(realm, roles));
		groupRepository.resetRealm(permissionService.iteratePrincipalsByRole(realm, roles));
	}
	
	@Override
	public void deleteRealm(Realm realm) {
		userRepository.deleteRealm(realm);
		groupRepository.deleteRealm(realm);
	}

	@Override
	public void verifyConnection(Realm realm) throws ResourceException {
	}

	public boolean supportsTemplates() {
		return true;
	}
	
	@JsonIgnore
	public ResourceTemplateRepository getTemplateRepository() {
		return templateRepository;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
