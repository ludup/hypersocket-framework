/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.hypersocket.tables.ColumnSort;

@Repository
@Transactional
public class LocalRealmProviderImpl extends AbstractRealmProvider implements
		LocalRealmProvider {

	private static Logger log = LoggerFactory
			.getLogger(LocalRealmProviderImpl.class);

	public final static String REALM_RESOURCE_CATEGORY = "local";
	public final static String USER_RESOURCE_CATEGORY = "localUser";
	public final static String GROUP_RESOURCE_CATEGORY = "localGroup";

	public final static String FIELD_FULLNAME = "user.fullname";
	public final static String FIELD_EMAIL = "user.email";
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

	@PostConstruct
	private void registerProvider() throws Exception {

		realmService.registerRealmProvider(this);

		loadPropertyTemplates("localRealmTemplate.xml");

		userRepository.loadPropertyTemplates("localUserTemplate.xml");

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
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceCreationException {

		try {
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

			return user;
		} catch (Exception e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.createFailed", username, e.getMessage());
		}

	}

	@Override
	public Principal updateUser(Realm realm, Principal principal,
			String username, Map<String, String> properties,
			List<Principal> principals) throws ResourceChangeException {

		try {

			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"principal is not of type LocalUser");
			}

			LocalUser user = (LocalUser) principal;
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
					"error.createFailed", principal.getPrincipalName(),
					e.getMessage());
		}

	}

	@Override
	public void setPassword(Principal principal, char[] password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {

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
	public void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {
		setPassword(principal, password.toCharArray(), forceChangeAtNextLogon);
	}

	@Override
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
	public boolean requiresPasswordChange(Principal principal) {
		if (principal instanceof LocalUser) {
			LocalUserCredentials creds = userRepository
					.getCredentials((LocalUser) principal);
			return creds == null || creds.isPasswordChangeRequired();
		} else {
			return false;
		}
	}

	@Override
	public Principal createGroup(Realm realm, String name,
			List<Principal> principals) throws ResourceCreationException {

		LocalGroup group = new LocalGroup();
		group.setName(name);
		group.setRealm(realm);

		userRepository.saveGroup(group);

		if (principals != null) {
			for (Principal principal : principals) {
				if (!(principal instanceof LocalUser)) {
					throw new IllegalStateException(
							"Principal is not of type LocalUser");
				}
				userRepository.assign((LocalUser) principal, group);
			}
		}
		return group;
	}

	@Override
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
	public Principal updateGroup(Realm realm, Principal group, String name,
			List<Principal> principals) throws ResourceChangeException {
		if (!(group instanceof LocalGroup)) {
			throw new IllegalStateException(
					"Principal is not of type LocalGroup");
		}

		LocalGroup grp = (LocalGroup) group;
		grp.setName(name);
		grp.getUsers().clear();

		for (Principal principal : principals) {
			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"Group member principal is not of type LocalUser");
			}
			grp.getUsers().add((LocalUser) principal);
		}

		userRepository.saveGroup(grp);

		return grp;
	}

	@Override
	public Principal createGroup(Realm realm, String name)
			throws ResourceCreationException {
		return createGroup(realm, name, null);
	}

	@Override
	public void deleteUser(Principal user) {

		if (!(user instanceof LocalUser)) {
			throw new IllegalStateException(
					"Principal is not of type LocalUser");
		}

		LocalUser usr = (LocalUser) user;

		userRepository.deleteUser(usr);
	}

	@Override
	public void deleteRealm(Realm realm) throws ResourceChangeException {

		for (Principal group : userRepository.allGroups(realm)) {
			deleteGroup(group);
		}

		for (Principal user : userRepository.allUsers(realm)) {
			deleteUser(user);
		}

	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal) {

		List<Principal> result = new ArrayList<Principal>();
		if (principal instanceof LocalUser) {
			result.addAll(userRepository.getGroupsByUser((LocalUser) principal));
		} else if (principal instanceof LocalGroup) {
			result.addAll(userRepository.getUsersByGroup((LocalGroup) principal));
		} else {
			throw new IllegalStateException(
					"Principal is not a LocalUser or LocalGroup!");
		}

		return result;

	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal,
			PrincipalType type) {

		List<Principal> result = new ArrayList<Principal>();

		switch (type) {
		case GROUP:
			if (principal instanceof LocalUser) {
				result.addAll(userRepository.getGroupsByUser((LocalUser)principal));
			}
			break;
		case USER:
			if (principal instanceof LocalGroup) {
				result.addAll(userRepository.getUsersByGroup((LocalGroup)principal));
			}
			break;
		default:
			// Nothing
		}

		return result;
	}

	@Override
	public Principal createUser(Realm realm, String username,
			Map<String, String> properties) throws ResourceCreationException {
		return createUser(realm, username, properties, null);
	}

	@Override
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
	public Collection<PropertyCategory> getUserProperties(Principal principal) {
		return userRepository.getPropertyCategories(principal);
	}

	@Override
	public Collection<PropertyCategory> getGroupProperties(Principal principal) {
		// TODO we need a way to get these from the repository - currently
		// restricted to only one resource type per repository
		return new ArrayList<PropertyCategory>();
	}

	@Override
	public Collection<PropertyCategory> getRealmProperties(Realm realm) {
		return getPropertyCategories(realm);
	}

//	@Override
//	public Set<Principal> getPrincipalsByProperty(String propertyName,
//			String propertyValue) {
//
//		Set<Principal> principals = new HashSet<Principal>();
//		List<DatabaseProperty> properties = userRepository
//				.getPropertiesWithValue(propertyName, propertyValue);
//		for (DatabaseProperty property : properties) {
//			principals.add((Principal) property.getResource());
//		}
//		return principals;
//	}

	@Override
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
					LocalRealmProviderImpl.FIELD_EMAIL);
			if (!StringUtils.isEmpty(phone)) {
				return phone;
			}
			break;
		}

		throw new MediaNotFoundException();
	}

	@Override
	public String getPrincipalDescription(Principal principal) {
		return getValue(principal, LocalRealmProviderImpl.FIELD_FULLNAME);
	}

}
