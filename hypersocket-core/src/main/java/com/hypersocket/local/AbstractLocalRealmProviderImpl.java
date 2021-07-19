/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.auth.PasswordEncryptionType;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyFilter;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.PasswordCreator;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalSuspension;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalSuspensionType;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.events.SessionOpenEvent;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.util.CompoundIterator;

public abstract class AbstractLocalRealmProviderImpl extends AbstractRealmProvider implements
		 ApplicationListener<SessionOpenEvent> {

	private static Logger log = LoggerFactory
			.getLogger(AbstractLocalRealmProviderImpl.class);

	public final static String USER_RESOURCE_CATEGORY = "localUser";
	public final static String GROUP_RESOURCE_CATEGORY = "localGroup";

	public final static String FIELD_FULLNAME = "fullname";
	public final static String FIELD_EMAIL = "email";
	public final static String FIELD_MOBILE = "mobile";
	public final static String FIELD_PASSWORD_ENCODING = "password.encoding";

	@Autowired
	private LocalUserRepository userRepository;

	@Autowired
	private LocalGroupRepository groupRepository;

	@Autowired
	private RealmService realmService;

	@Autowired
	private PasswordEncryptionService encryptionService;
	
	@Autowired
	private PrincipalSuspensionService suspensionService; 
	
	@Autowired
	private ConfigurationService configurationService;

	private Set<String> defaultProperties = new HashSet<String>();
	
	@PostConstruct
	private void registerProvider() throws Exception {

		defaultProperties.add("fullname");
		defaultProperties.add("email");
		defaultProperties.add("mobile");
		
		realmService.registerRealmProvider(this);

		loadPropertyTemplates("localRealmTemplate.xml");

		userRepository.loadPropertyTemplates("localUserTemplate.xml");
		userRepository.registerPropertyResolver(userAttributeService.getPropertyResolver());
		groupRepository.loadPropertyTemplates("localGroupTemplate.xml");
	}


	@Override
	public abstract String getResourceBundle();

	
	@Override
	public Iterator<Principal> iterateAllPrincipals(Realm realm, PrincipalType... types) {
		CompoundIterator<Principal> compoundIterator = new CompoundIterator<>();
		for (PrincipalType type : types) {
			switch (type) {
			case USER:
				compoundIterator.addIterator(userRepository.iterateUsers(realm, new ColumnSort[0]));
				break;
			case GROUP:
				compoundIterator.addIterator(groupRepository.iterateGroups(realm, new ColumnSort[0]));
				break;
			case SERVICE:
			case SYSTEM:
			case TEMPLATE:
				break;
			}
		}
		return compoundIterator;
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
				principal = groupRepository.getGroupByName(principalName, realm);
				break;
			case SERVICE:
			case SYSTEM:
			case TEMPLATE:
				principal = userRepository.getUserByNameAndType(principalName,
						realm, type);
				break;
				
			}
			if (principal != null)
				break;
		}
		return principal;
	}
	
	@Override
	public Principal getPrincipalByEmail(Realm realm, String email) {
		return userRepository.getUserByEmail(email, realm);
	}
	
	@Override
	public UserPrincipal<?> getPrincipalByFullName(Realm realm, String fullName) {
		return userRepository.getUserByFullName(fullName, realm);
	}

	@Override
	@Transactional(readOnly=true)
	public boolean verifyPassword(Principal principal, char[] password) {

		if (log.isDebugEnabled()) {
			log.debug("Verifying password for "
					+ principal.getType().toString() + " principal "
					+ principal.getPrincipalName());
		}

		if (principal.getType() != PrincipalType.USER && principal.getType() != PrincipalType.SERVICE) {
			if (log.isDebugEnabled()) {
				log.debug("Principal " + principal.getPrincipalName()
						+ " is not a user or a service! cannot verify password");
			}
			return false;
		}

		LocalUser user = (LocalUser) principal;

		LocalUserCredentials creds = userRepository.getCredentials(user);
		if(creds == null) {
			/* If there are NO credentials, the record has been deleted from the database as a last
			 * resort recovery method to regain access if the administrator password has been lost
			 * of the encryption systems is failing to decrypt secrets for some reason. Doing this
			 * will trigger a password change upon login.
			 */
			log.warn(String.format("Allow credentialess user to login.", user.getPrincipalName()));
			return true;
		}

		try {
			return encryptionService.authenticate(password,
					Base64.decodeBase64(creds.getEncodedPassword()), 
					Base64.decodeBase64(creds.getEncodedSalt()),
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
			PasswordCreator passwordCreator, boolean forceChange, PrincipalType type)
			throws ResourceException {
		
		try {
			
			Principal principal = getPrincipalByName(username,
					realm, 
					PrincipalType.USER, PrincipalType.SYSTEM, PrincipalType.SERVICE);
			
			if(principal!=null) {
				throw new ResourceCreationException(getResourceBundle(), "error.principalExists", username);
			}
			
			LocalUser user = new LocalUser();
			user.setName(username);
			user.setResourceCategory(USER_RESOURCE_CATEGORY);
			user.setRealm(realm);
			user.setPrincipalType(type);
			user.setPosixId(userRepository.getNextPosixId(realm));
			user.setType(type);

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

			checkExpiry(user);
			
			if(passwordCreator!=null) {
				setPassword(user, passwordCreator.createPassword(user), forceChange, true);
			}
			return user;
		} catch (Exception e) {
			throw new ResourceCreationException(getResourceBundle(),
					"error.createFailed", username, e.getMessage());
		}

	}

	protected void checkExpiry(LocalUser user) throws ResourceNotFoundException, ResourceException, AccessDeniedException {

		PrincipalSuspension suspension = suspensionService.getSuspension(user.getPrincipalName(), user.getRealm(), PrincipalSuspensionType.EXPIRY);

		if(suspension!=null) {
			suspensionService.deletePrincipalSuspension(user, PrincipalSuspensionType.EXPIRY);
		} 

		if(user.getExpires()!=null) {
			suspensionService.createPrincipalSuspension(user, user.getPrincipalName(), user.getRealm(), user.getExpires(), 0L, PrincipalSuspensionType.EXPIRY);
		}
	}
	
	@Override
	@Transactional
	public Principal updateUser(Realm realm, Principal principal,
			String username, Map<String, String> properties,
			List<Principal> principals) throws ResourceException {

		try {

			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"principal is not of type LocalUser");
			}

			// Get again so we have it within a transaction so lazy loading works.
			LocalUser user = (LocalUser) userRepository.getUserById(principal.getId(), principal.getRealm(), false);
			user.setName(username);
			user.setRealm(realm);

			if(Objects.nonNull(principals)) {
				user.getGroups().clear();
	
				for (Principal p : principals) {
					if (p instanceof LocalGroup) {
						user.getGroups().add((LocalGroup) p);
					}
				}
			}
			
			userRepository.saveUser(user, properties);

			userRepository.flush();
			userRepository.refresh(user);

			checkExpiry(user);
			
			return user;
		} catch (Exception e) {
			throw new ResourceChangeException(getResourceBundle(),
					"error.updateFailed", principal.getPrincipalName(),
					e.getMessage(), e);
		}
	}
	
	@Override
	@Transactional
	public Principal updateUserProperties(Principal principal,
			Map<String, String> properties) throws ResourceException {

		try {

			if (!(principal instanceof LocalUser)) {
				throw new IllegalStateException(
						"principal is not of type LocalUser");
			}

			// Get again so we have it within a transaction so lazy loading works.
			LocalUser user = (LocalUser) userRepository.getUserById(principal.getId(), principal.getRealm(), false);

			userRepository.saveUser(user, properties);

			userRepository.flush();
			userRepository.refresh(user);

			return user;
		} catch (Exception e) {
			throw new ResourceChangeException(e, getResourceBundle(),
					"error.updateFailed", principal.getPrincipalName(),
					e.getMessage());
		}

	}

	@Override
	@Transactional
	public void changePassword(Principal principal, char[] oldPassword,
			char[] newPassword) throws ResourceException,
			ResourceCreationException {
		if(!verifyPassword(principal, oldPassword)) {
			throw new ResourceChangeException(getResourceBundle(), "invalid.password");
		}
		
		setPassword(principal, newPassword, false, false);
	}
	
	@Override
	@Transactional
	public void setPassword(Principal principal, char[] password,
			boolean forceChangeAtNextLogon, boolean administrative) throws ResourceException {

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
					.valueOf(configurationService.getValue(principal.getRealm(), "password.encoding"));
			byte[] encryptedPassword = encryptionService.getEncryptedPassword(
					password, salt, passwordEncoding);

			creds.setUser(localUser);
			creds.setEncryptionType(passwordEncoding);
			creds.setEncodedPassword(Base64.encodeBase64String(encryptedPassword));
			creds.setEncodedSalt(Base64.encodeBase64String(salt));
			creds.setPasswordChangeRequired(forceChangeAtNextLogon);
			creds.setPassword(encryptedPassword);
			creds.setSalt(salt);
			
			userRepository.saveCredentials(creds);

		} catch (Throwable e) {
			throw new ResourceCreationException(getResourceBundle(),
					"error.creatingPassword", principal.getPrincipalName(),
					e.getMessage(),
					e);
		}
	}

	@Override
	@Transactional
	public void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon, boolean administrative) throws ResourceException {
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
				principal = userRepository.getUserById(id, realm, false);
				break;
			case GROUP:
				principal = groupRepository.getGroupById(id, realm, false);
				break;
			case SERVICE:
			case SYSTEM:
			case TEMPLATE:
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
	public Principal getDeletedPrincipalById(Long id, Realm realm,
			PrincipalType[] acceptTypes) {

		Principal principal = null;
		for (PrincipalType type : acceptTypes) {
			switch (type) {
			case USER:
				principal = userRepository.getUserById(id, realm, true);
				break;
			case GROUP:
				principal = groupRepository.getGroupById(id, realm, true);
				break;
			case SERVICE:
			case SYSTEM:
			case TEMPLATE:
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
			Map<String, String> properties, List<Principal> principals, List<Principal> groups) throws ResourceException {

		LocalGroup group = new LocalGroup();
		group.setName(name);
		group.setRealm(realm);
		group.setPrincipalType(PrincipalType.GROUP);
		group.setPosixId(groupRepository.getNextPosixId(realm));
		

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
		
		groupRepository.saveGroup(group);
		
		return group;
	}

	@Override
	@Transactional
	public void deleteGroup(Principal group, boolean deleteLocallyOnly) throws ResourceException {

		if (!(group instanceof LocalGroup)) {
			throw new IllegalStateException(
					"Principal is not of type LocalGroup");
		}
		LocalGroup grp = (LocalGroup) group;
		grp.getUsers().clear();
		groupRepository.deleteGroup(grp);

	}

	@Override
	@Transactional
	public Principal updateGroup(Realm realm, Principal group, String name,
			Map<String, String> properties, List<Principal> principals, List<Principal> groups) throws ResourceException {
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
				throw new ResourceChangeException(getResourceBundle(), "error.groupContainsSelf", principal.getName());
			}
			grp.getGroups().add((LocalGroup) principal);
		}

		groupRepository.saveGroup(grp);

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
			throws ResourceException {
		return createGroup(realm, name, properties, null, null);
	}

	@Override
	@Transactional
	public void deleteUser(Principal user, boolean deleteLocallyOnly) {

		if (!(user instanceof LocalUser)) {
			throw new IllegalStateException(
					"Principal is not of type LocalUser");
		}

		LocalUser usr = (LocalUser) user;

		userRepository.deleteUser(usr);
	}

	@Override
	@Transactional
	public void deleteRealm(Realm realm) throws ResourceException {
		userRepository.deleteRealm(realm);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Principal> getAssociatedPrincipals(Principal principal) {

		List<Principal> result = new ArrayList<Principal>();
		if (principal instanceof LocalUser) {
			LocalUser user = (LocalUser) principal;
			result.addAll(user.getGroups());
			
			for(LocalGroup group : user.getGroups()) {
				iterateParentGroups(group, result, null);
			}
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
	
	@Override
	public List<Principal> getUserGroups(Principal principal) {
		return new ArrayList<Principal>(((LocalUser)principal).getGroups());
	}

	@Override
	public List<Principal> getGroupUsers(Principal principal) {
		return new ArrayList<Principal>(((LocalGroup)principal).getUsers());
	}

	@Override
	public List<Principal> getGroupGroups(Principal principal) {
		return new ArrayList<Principal>(((LocalGroup)principal).getGroups());
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
	
	private void iterateParentGroups(LocalGroup group, List<Principal> principals, Set<LocalGroup> iterated) {
		if(iterated==null) {
			iterated = new HashSet<LocalGroup>();
		}
		if(iterated.contains(group)) {
			return;
		}
		iterated.add(group);
		for(LocalGroup child : group.getParents()) {
			principals.add(child);
			iterateParentGroups(child, principals, iterated);
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
			String searchColumn, 
			String searchPattern) {
		switch (type) {
		case USER:
			return userRepository.countUsers(realm, searchColumn, searchPattern);
		case GROUP:
			return groupRepository.countGroups(realm, searchColumn, searchPattern);
		default:
			return 0L;
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<?> getPrincipals(Realm realm, PrincipalType type,
			String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting) {

		switch (type) {
		case USER:
			return userRepository.getUsers(realm, searchColumn, searchPattern, start, length,
					sorting);
		case GROUP:
			return groupRepository.getGroups(realm, searchColumn, searchPattern, start,
					length, sorting);
		default:
			throw new IllegalArgumentException(
					"Invalid principal type passed to AbstractRemoteRealmProviderImpl");
		}

	}

	@Override
	@Transactional(readOnly=true)
	public Collection<PropertyCategory> getUserProperties(RealmResource principal) {
		return userRepository.getPropertyCategories(principal);
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<PropertyCategory> getGroupProperties(Principal principal) {
		return groupRepository.getPropertyCategories(principal);
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
			String email = ((LocalUser)principal).getEmail();
			if (!StringUtils.isEmpty(email)) {
				return email;
			}
			/**
			 * Look for older attribute
			 */
			email = userRepository.getValue(principal, "user.email");
			if (!StringUtils.isEmpty(email)) {
				return email;
			}
			break;
		case PHONE:
			String phone = ((LocalUser)principal).getMobile();
			if (!StringUtils.isEmpty(phone)) {
				return phone;
			}
			/**
			 * Look for older attribute
			 */
			phone = userRepository.getValue(principal,
					"user.mobile");
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
		return getValue(principal, AbstractLocalRealmProviderImpl.FIELD_FULLNAME);
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
			throws ResourceException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal enableAccount(Principal principal)
			throws ResourceException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal unlockAccount(Principal principal)
			throws ResourceException {
		throw new UnsupportedOperationException();
	}

	@Override
	@Transactional(readOnly=true)
	public Set<String> getUserPropertyNames(Principal principal) {
		return userRepository.getPropertyNames(principal);
	}

	@Override
	public Set<String> getGroupPropertyNames(Principal principal) {
		return groupRepository.getPropertyNames(principal);
	}

	@Override
	public void testConnection(Map<String, String> properties, TestConnectionRetry retry)
			throws IOException {
		
	}

	@Override
	public void testConnection(Map<String, String> properties, Realm realm, TestConnectionRetry retry) {
		
	}
	
	@Override
	@Transactional(readOnly=true)
	public String getUserPropertyValue(Principal principal, String name) {
		PropertyTemplate template = userRepository.getPropertyTemplate(principal, name);
		if(template==null || !template.isEncrypted()) {
			return userRepository.getValue(principal, name);
		}
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
	public Long getUserPropertyLong(Principal principal, String resourceKey) {
		return userRepository.getLongValue(principal, resourceKey);
	}
	
	@Override
	public Integer getUserPropertyInt(Principal principal, String resourceKey) {
		return userRepository.getIntValue(principal, resourceKey);
	}
	
	@Override
	public boolean getUserPropertyBoolean(Principal principal, String resourceKey) {
		return userRepository.getBooleanValue(principal, resourceKey);
	}
	
	@Override
	public String getUserProperty(Principal principal, String resourceKey) {
		return userRepository.getValue(principal, resourceKey);
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
	public void onApplicationEvent(final SessionOpenEvent event) {
	
		if(event.getTargetPrincipal() instanceof LocalUser) {
			
			LocalUser user = (LocalUser) event.getTargetPrincipal();
			user.setLastSignOn(new Date(event.getTimestamp()));
			userRepository.saveUser(user, new HashMap<String,String>());
		}		
	}

	private Collection<String> convertNames(Collection<PropertyCategory> cats) {
		Set<String> names = new HashSet<String>();
		for(PropertyCategory c : cats) {
			for(AbstractPropertyTemplate t : c.getTemplates()) {
				names.add(t.getResourceKey());
			}
		}
		return names;
	}
	
	@Override
	public Collection<String> getEditablePropertyNames(Realm realm) {
		final boolean editable = !isReadOnly(realm);
		return convertNames(userRepository.getPropertyCategories(null, new PropertyFilter() {
			@Override
			public boolean filterProperty(AbstractPropertyTemplate t) {
				if("true".equals(t.getAttributes().get("userAttribute"))) {
					return !t.isReadOnly();
				}
				return editable && !t.isReadOnly() && !t.isHidden() && !"hidden".equals(t.getAttributes().get("inputType"));
			}
		}));
	}

	@Override
	public Collection<String> getVisiblePropertyNames(Realm realm) {
		return convertNames(userRepository.getPropertyCategories(null, new PropertyFilter() {

			@Override
			public boolean filterProperty(AbstractPropertyTemplate t) {
				if("true".equals(t.getAttributes().get("userAttribute"))) {
					return true;
				}
				return !t.isHidden() && !"hidden".equals(t.getAttributes().get("inputType"));
			}
			
		}));
	}
}
