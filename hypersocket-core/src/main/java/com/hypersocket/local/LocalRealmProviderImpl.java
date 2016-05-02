/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.auth.PasswordEncryptionType;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.session.events.SessionOpenEvent;
import com.hypersocket.tables.ColumnSort;

@Repository
public class LocalRealmProviderImpl extends AbstractRealmProvider implements
		LocalRealmProvider, ApplicationListener<SessionOpenEvent> {

	private static Logger log = LoggerFactory
			.getLogger(LocalRealmProviderImpl.class);

	public final static String REALM_RESOURCE_CATEGORY = "local";
	public final static String USER_RESOURCE_CATEGORY = "localUser";
	public final static String GROUP_RESOURCE_CATEGORY = "localGroup";

	public final static String FIELD_FULLNAME = "user.fullname";
	public final static String FIELD_EMAIL = "user.email";
	public final static String FIELD_MOBILE = "user.mobile";
	public final static String FIELD_PASSWORD_ENCODING = "password.encoding";

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	RealmService realmService;

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	PasswordEncryptionService encryptionService;
	
	PropertyCategory userDetailsCategory;

	Set<String> defaultProperties = new HashSet<String>();
	
	@PostConstruct
	private void registerProvider() throws Exception {

		defaultProperties.add("user.fullname");
		defaultProperties.add("user.email");
		defaultProperties.add("user.mobile");
		
		realmService.registerRealmProvider(this);

		loadPropertyTemplates("localRealmTemplate.xml");

		userRepository.loadPropertyTemplates("localUserTemplate.xml");
		userRepository.registerPropertyResolver(userAttributeService);
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
	@Transactional(readOnly=true)
	public List<Principal> allPrincipals(Realm realm, PrincipalType... types) {

		ArrayList<Principal> result = new ArrayList<Principal>();

		for (PrincipalType type : types) {
			switch (type) {
			case USER:
				result.addAll(userRepository.allUsers(realm));
				break;
			case GROUP:
				result.addAll(userRepository.allGroups(realm));
				break;
			case SERVICE:
				break;
			case SYSTEM:
				break;
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getPrincipalByName(String principalName, Realm realm,
			PrincipalType... acceptTypes) {

		Principal principal = null;
		for (PrincipalType type : acceptTypes) {
			switch (type) {
			case USER:
				principal = userRepository.getUserByName(principalName, realm);
				break;
			case GROUP:
				principal = userRepository.getGroupByName(principalName, realm);
				break;
			case SERVICE:
				principal = userRepository.getUserByNameAndType(principalName, realm, PrincipalType.SERVICE);
				break;
			case SYSTEM:
				principal = userRepository.getUserByNameAndType(principalName,
						realm, PrincipalType.SYSTEM);
				break;
			}
			if (principal != null)
				break;
		}
		return principal;
	}

	@Override
	@Transactional(readOnly=true)
	public boolean verifyPassword(Principal principal, char[] password) {

		if (log.isDebugEnabled()) {
			log.debug("Verifying password for "
					+ principal.getType().toString() + " principal "
					+ principal.getPrincipalName());
		}

		if (principal.getType() != PrincipalType.USER) {
			if (log.isDebugEnabled()) {
				log.debug("Principal " + principal.getPrincipalName()
						+ " is not a user! cannot verify password");
			}
			return false;
		}

		LocalUser user = (LocalUser) principal;

		LocalUserCredentials creds = userRepository.getCredentials(user);

		try {
			return encryptionService.authenticate(password,
					creds.getPassword(), creds.getSalt(),
					creds.getEncryptionType());
		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.error("Failed to verify password", e);
			}
			return false;
		}
	}

	@Override
	public boolean isReadOnly(Realm realm) {
		return false;
	}

	@Override
	@Transactional
	public Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals,
			String password, boolean forceChange)
			throws ResourceCreationException {
		
		try {
			
			Principal principal = getPrincipalByName(username,
					realm, 
					PrincipalType.USER, PrincipalType.SYSTEM, PrincipalType.SERVICE);
			
			if(principal!=null) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.principalExists", username);
			}
			
			LocalUser user = new LocalUser();
			user.setName(username);
			user.setResourceCategory(USER_RESOURCE_CATEGORY);
			user.setRealm(realm);

			if (principals != null) {
				for (Principal p : principals) {
					if (p instanceof LocalGroup) {
						user.getGroups().add((LocalGroup) p);
					}
				}
			}

			userRepository.saveUser(user, properties);

			userRepository.flush();
			userRepository.refresh(user);

			if(password!=null) {
				setPassword(user, password, forceChange, true);
			}
			return user;
		} catch (Exception e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.createFailed", username, e.getMessage());
		}

	}

	@Override
	@Transactional
	public Principal updateUser(Realm realm, Principal principal,
			String username, Map<String, String> properties,
			List<Principal> principals) throws ResourceChangeException {

		try {

			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"principal is not of type LocalUser");
			}

			// Get again so we have it within a transaction so lazy loading works.
			LocalUser user = (LocalUser) userRepository.getUserById(principal.getId(), principal.getRealm());
			user.setName(username);
			user.setRealm(realm);

			user.getGroups().clear();

			for (Principal p : principals) {
				if (p instanceof LocalGroup) {
					user.getGroups().add((LocalGroup) p);
				}
			}
			
			userRepository.saveUser(user, properties);

			userRepository.flush();
			userRepository.refresh(user);

			return user;
		} catch (Exception e) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.updateFailed", principal.getPrincipalName(),
					e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public Principal updateUserProperties(Principal principal,
			Map<String, String> properties) throws ResourceChangeException {

		try {

			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"principal is not of type LocalUser");
			}

			// Get again so we have it within a transaction so lazy loading works.
			LocalUser user = (LocalUser) userRepository.getUserById(principal.getId(), principal.getRealm());

			userRepository.saveUser(user, properties);

			userRepository.flush();
			userRepository.refresh(user);

			return user;
		} catch (Exception e) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.updateFailed", principal.getPrincipalName(),
					e.getMessage());
		}

	}

	@Override
	@Transactional
	public void changePassword(Principal principal, char[] oldPassword,
			char[] newPassword) throws ResourceChangeException,
			ResourceCreationException {
		if(!verifyPassword(principal, oldPassword)) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "invalid.password");
		}
		
		setPassword(principal, newPassword, false, false);
	}
	
	@Override
	@Transactional
	public void setPassword(Principal principal, char[] password,
			boolean forceChangeAtNextLogon, boolean administrative) throws ResourceCreationException {

		if (!(principal instanceof LocalUser)) {
			throw new IllegalStateException("Principal is not a LocalUser");
		}

		LocalUser localUser = (LocalUser) principal;

		LocalUserCredentials creds = userRepository.getCredentials(localUser);

		if (creds == null) {
			creds = new LocalUserCredentials();
		}

		try {
			byte[] salt = encryptionService.generateSalt();
			PasswordEncryptionType passwordEncoding = PasswordEncryptionType
					.valueOf(getValue(principal.getRealm(), "password.encoding"));
			byte[] encryptedPassword = encryptionService.getEncryptedPassword(
					password, salt, passwordEncoding);

			creds.setUser(localUser);
			creds.setEncryptionType(passwordEncoding);
			creds.setPassword(encryptedPassword);
			creds.setSalt(salt);
			creds.setPasswordChangeRequired(forceChangeAtNextLogon);

			userRepository.saveCredentials(creds);

		} catch (Throwable e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.creatingPassword", principal.getPrincipalName(),
					e.getMessage());
		}
	}

	@Override
	@Transactional
	public void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon, boolean administrative) throws ResourceCreationException {
		setPassword(principal, password.toCharArray(), forceChangeAtNextLogon, administrative);
	}

	@Override
	@Transactional(readOnly=true)
	public Principal getPrincipalById(Long id, Realm realm,
			PrincipalType[] acceptTypes) {

		Principal principal = null;
		for (PrincipalType type : acceptTypes) {
			switch (type) {
			case USER:
				principal = userRepository.getUserById(id, realm);
				break;
			case GROUP:
				principal = userRepository.getGroupById(id, realm);
				break;
			case SERVICE:
				break;
			case SYSTEM:
				principal = userRepository.getUserByIdAndType(id, realm,
						PrincipalType.SYSTEM);
				break;
			}
			if (principal != null)
				break;
		}
		return principal;
	}

	@Override
	@Transactional(readOnly=true)
	public boolean requiresPasswordChange(Principal principal) {
		if (principal instanceof LocalUser) {
			if(principal.getType().equals(PrincipalType.SERVICE)) {
				return false;
			}
			LocalUserCredentials creds = userRepository
					.getCredentials((LocalUser) principal);
			return creds == null || creds.isPasswordChangeRequired();
		} else {
			return false;
		}
	}

	@Override
	@Transactional
	public Principal createGroup(Realm realm, String name,
			Map<String, String> properties, List<Principal> principals, List<Principal> groups) throws ResourceCreationException {

		LocalGroup group = new LocalGroup();
		group.setName(name);
		group.setRealm(realm);

		

		if (principals != null) {
			for (Principal principal : principals) {
				if (!(principal instanceof LocalUser)) {
					throw new IllegalStateException(
							"Principal is not of type LocalUser");
				}
				group.getUsers().add((LocalUser)principal);
			}
		}
		
		if (groups != null) {
			for (Principal principal : groups) {
				if (!(principal instanceof LocalGroup)) {
					throw new IllegalStateException(
							"Principal is not of type LocalGroup");
				}
				group.getGroups().add((LocalGroup)principal);
			}
		}
		
		userRepository.saveGroup(group);
		
		return group;
	}

	@Override
	@Transactional
	public void deleteGroup(Principal group) throws ResourceChangeException {

		if (!(group instanceof LocalGroup)) {
			throw new IllegalStateException(
					"Principal is not of type LocalGroup");
		}
		LocalGroup grp = (LocalGroup) group;
		grp.getUsers().clear();
		userRepository.deleteGroup(grp);

	}

	@Override
	@Transactional
	public Principal updateGroup(Realm realm, Principal group, String name,
			Map<String, String> properties, List<Principal> principals, List<Principal> groups) throws ResourceChangeException {
		if (!(group instanceof LocalGroup)) {
			throw new IllegalStateException(
					"Principal is not of type LocalGroup");
		}

		LocalGroup grp = (LocalGroup) group;
		grp.setName(name);
		grp.getUsers().clear();
		grp.getGroups().clear();
		
		for (Principal principal : principals) {
			if (principal!=null && !(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"Group member principal is not of type LocalUser");
			}
			grp.getUsers().add((LocalUser) principal);
		}
		
		for (Principal principal : groups) {
			if (principal!=null && !(principal instanceof LocalGroup)) {
				throw new IllegalStateException(
						"Group member principal is not of type LocalGroup");
			}
			if(containsSelf((LocalGroup)group, (LocalGroup) principal)) {
				throw new ResourceChangeException(LocalRealmProviderImpl.RESOURCE_BUNDLE, "error.groupContainsSelf", principal.getName());
			}
			grp.getGroups().add((LocalGroup) principal);
		}

		userRepository.saveGroup(grp);

		return grp;
	}
	
	private boolean containsSelf(LocalGroup self, LocalGroup group) {
		if(group.equals(self)) {
			return true;
		}
		for(LocalGroup child : group.getGroups()) {
			if(containsSelf(self, child)) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional
	public Principal createGroup(Realm realm, String name, Map<String, String> properties)
			throws ResourceCreationException {
		return createGroup(realm, name, properties, null, null);
	}

	@Override
	@Transactional
	public void deleteUser(Principal user) {

		if (!(user instanceof LocalUser)) {
			throw new IllegalStateException(
					"Principal is not of type LocalUser");
		}

		LocalUser usr = (LocalUser) user;

		userRepository.deleteUser(usr);
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) throws ResourceChangeException {

		for (Principal group : userRepository.allGroups(realm)) {
			deleteGroup(group);
		}

		for (Principal user : userRepository.allUsers(realm)) {
			deleteUser(user);
		}

	}

	@Override
	@Transactional(readOnly=true)
	public List<Principal> getAssociatedPrincipals(Principal principal) {

		List<Principal> result = new ArrayList<Principal>();
		if (principal instanceof LocalUser) {
			LocalUser user = (LocalUser) principal;
			result.addAll(user.getGroups());
		} else if (principal instanceof LocalGroup) {
			LocalGroup group = (LocalGroup) principal;
			result.addAll(group.getUsers());
			iterateGroups(group, result, null);
		} else {
			throw new IllegalStateException(
					"Principal is not a LocalUser or LocalGroup!");
		}

		return result;

	}
	
	private void iterateGroups(LocalGroup group, List<Principal> users, Set<LocalGroup> iterated) {
		if(iterated==null) {
			iterated = new HashSet<LocalGroup>();
		}
		if(iterated.contains(group)) {
			return;
		}
		iterated.add(group);
		for(LocalGroup child : group.getGroups()) {
			users.addAll(child.getUsers());
			iterateGroups(child, users, iterated);
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<Principal> getAssociatedPrincipals(Principal principal,
			PrincipalType type) {

		List<Principal> result = new ArrayList<Principal>();

		switch (type) {
		case GROUP:
			if (principal instanceof LocalUser) {
				result.addAll(((LocalUser)principal).getGroups());
			} else if(principal instanceof LocalGroup) {
				result.addAll(((LocalGroup)principal).getGroups());
			}
			break;
		case USER:
			if (principal instanceof LocalGroup) {
				LocalGroup group = (LocalGroup) principal;
				result.addAll(group.getUsers());
				iterateGroups(group, result, null);
			}
			break;
		default:
			// Nothing
		}

		return result;
	}

	@Override
	@Transactional(readOnly=true)
	public Long getPrincipalCount(Realm realm, PrincipalType type,
			String searchPattern) {
		switch (type) {
		case USER:
			return userRepository.countUsers(realm, searchPattern);
		case GROUP:
			return userRepository.countGroups(realm, searchPattern);
		default:
			return 0L;
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<?> getPrincipals(Realm realm, PrincipalType type,
			String searchPattern, int start, int length, ColumnSort[] sorting) {

		switch (type) {
		case USER:
			return userRepository.getUsers(realm, searchPattern, start, length,
					sorting);
		case GROUP:
			return userRepository.getGroups(realm, searchPattern, start,
					length, sorting);
		default:
			throw new IllegalArgumentException(
					"Invalid principal type passed to AbstractRemoteRealmProviderImpl");
		}

	}

	@Override
	@Transactional(readOnly=true)
	public Collection<PropertyCategory> getUserProperties(Principal principal) {
		return userRepository.getPropertyCategories(principal);
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<PropertyCategory> getGroupProperties(Principal principal) {
		// TODO we need a way to get these from the repository - currently
		// restricted to only one resource type per repository
		return new ArrayList<PropertyCategory>();
	}

	@Override
	public Collection<PropertyCategory> getRealmProperties(Realm realm) {
		return getPropertyCategories(realm);
	}

	@Override
	@Transactional(readOnly=true)
	public String getAddress(Principal principal, MediaType type)
			throws MediaNotFoundException {

		switch (type) {
		case EMAIL:
			String email = userRepository.getValue(principal,
					LocalRealmProviderImpl.FIELD_EMAIL);
			if (!StringUtils.isEmpty(email)) {
				return email;
			}
			break;
		case PHONE:
			String phone = userRepository.getValue(principal,
					LocalRealmProviderImpl.FIELD_MOBILE);
			if (!StringUtils.isEmpty(phone)) {
				return phone;
			}
			break;
		}

		throw new MediaNotFoundException();
	}

	@Override
	@Transactional(readOnly=true)
	public String getPrincipalDescription(Principal principal) {
		return getValue(principal, LocalRealmProviderImpl.FIELD_FULLNAME);
	}

	@Override
	@Transactional(readOnly=true)
	public boolean supportsAccountUnlock(Realm realm) {
		return false;
	}

	@Override
	public boolean supportsAccountDisable(Realm realm) {
		return false;
	}
	
	@Override
	public boolean isDisabled(Principal principal) {
		return false;
	}

	@Override
	public Principal disableAccount(Principal principal)
			throws ResourceChangeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal enableAccount(Principal principal)
			throws ResourceChangeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal unlockAccount(Principal principal)
			throws ResourceChangeException {
		throw new UnsupportedOperationException();
	}

	@Override
	@Transactional(readOnly=true)
	public Set<String> getUserPropertyNames(Principal principal) {
		return userRepository.getPropertyNames(principal);
	}

	@Override
	public Set<String> getGroupPropertyNames(Principal principal) {
		return new HashSet<String>();
	}

	@Override
	public void testConnection(Map<String, String> properties)
			throws IOException {
		
	}

	@Override
	public void testConnection(Map<String, String> properties, Realm realm)
			throws IOException {
		
	}
	
	@Override
	@Transactional(readOnly=true)
	public String getUserPropertyValue(Principal principal, String name) {
		return userRepository.getDecryptedValue(principal, name);
	}

	@Override
	@Transactional(readOnly=true)
	public Set<String> getUserVariableNames(Principal principal) {
		return userRepository.getVariableNames(principal);
	}

	@Override
	@Transactional(readOnly=true)
	public Map<String, String> getUserPropertyValues(Principal principal) {
		return userRepository.getProperties(principal);
	}

	@Override
	@Transactional(readOnly=true)
	public Set<String> getDefaultUserPropertyNames() {
		return defaultProperties;
	}

	@Override
	@Transactional(readOnly=true)
	public boolean hasPropertyValueSet(Principal principal, String resourceKey) {
		return userRepository.hasPropertyValueSet(principal, resourceKey);
	}

	@Override
	@Transactional(readOnly=true)
	public String getDecryptedValue(Realm realm, String resourceKey) {
		return getDecryptedValue(realm, resourceKey);
	}

	@Override
	public boolean canChangePassword(Principal principal) {
		return true;
	}

	@Override
	public void onApplicationEvent(final SessionOpenEvent event) {
	
		if(event.getPrincipal() instanceof LocalUser) {
			
			LocalUser user = (LocalUser) event.getPrincipal();
			user.setLastSignOn(new Date(event.getTimestamp()));
			userRepository.saveUser(user, new HashMap<String,String>());
		}
				
	}

}
