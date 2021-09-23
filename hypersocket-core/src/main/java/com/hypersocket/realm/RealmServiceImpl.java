/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.attributes.AttributeType;
import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeService;
import com.hypersocket.auth.FakePrincipal;
import com.hypersocket.auth.PasswordEnabledAuthenticatedServiceImpl;
import com.hypersocket.cache.CacheService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.delegation.UserDelegationResourceService;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.events.EventService;
import com.hypersocket.export.AbstractPagingExportDataProvider;
import com.hypersocket.export.CommonEndOfLine;
import com.hypersocket.export.CommonEndOfLineEnum;
import com.hypersocket.export.ExportService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.local.LocalRealmProviderImpl;
import com.hypersocket.local.LocalUser;
import com.hypersocket.message.MessageResourceService;
import com.hypersocket.password.policy.PasswordPolicyResourceService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.PermissionScope;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.events.AccountDisabledEvent;
import com.hypersocket.realm.events.AccountEnabledEvent;
import com.hypersocket.realm.events.ChangePasswordEvent;
import com.hypersocket.realm.events.ExternalPasswordEvent;
import com.hypersocket.realm.events.GroupCreatedEvent;
import com.hypersocket.realm.events.GroupDeletedEvent;
import com.hypersocket.realm.events.GroupEvent;
import com.hypersocket.realm.events.GroupUpdatedEvent;
import com.hypersocket.realm.events.PasswordUpdateEvent;
import com.hypersocket.realm.events.PrincipalEvent;
import com.hypersocket.realm.events.ProfileUpdatedEvent;
import com.hypersocket.realm.events.RealmCreatedEvent;
import com.hypersocket.realm.events.RealmDeletedEvent;
import com.hypersocket.realm.events.RealmEvent;
import com.hypersocket.realm.events.RealmUpdatedEvent;
import com.hypersocket.realm.events.ResetPasswordEvent;
import com.hypersocket.realm.events.SetPasswordEvent;
import com.hypersocket.realm.events.UserCreatedEvent;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.realm.events.UserEvent;
import com.hypersocket.realm.events.UserUndeletedEvent;
import com.hypersocket.realm.events.UserUpdatedEvent;
import com.hypersocket.realm.ou.OrganizationalUnitRepository;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractSimpleResourceRepository;
import com.hypersocket.resource.FindableResourceRepository;
import com.hypersocket.resource.PropertyChange;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.ResourcePassthroughException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DefaultTableFilter;
import com.hypersocket.tables.TableFilter;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;
import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class RealmServiceImpl extends PasswordEnabledAuthenticatedServiceImpl
		implements RealmService, UpgradeServiceListener {
	
	private static final String TEXT_PRINCIPAL_NAME = "text.principalName";
	private static final String TEXT_REALM = "text.realm";
	private static final String TEXT_NAME = "text.name";
	private static final String TEXT_EMAIL = "text.email";
	private static final String TEXT_OU = "text.ou";
	private static final String TEXT_PRIMARY_EMAIL = "text.primaryEmail";
	private static final String TEXT_DESCRIPTION = "text.description";
	private static final String TEXT_UUID = "text.uuid";
	private static final String TEXT_CREATE_DATE = "text.createDate";
	private static final String TEXT_MODIFIED_DATE = "text.modifiedDate";
	private static final String TEXT_EXPIRES = "text.expires";
	private static final String TEXT_STATUS = "text.status";
	private static final String TEXT_LAST_PASSWORD_CHANGE = "text.lastPasswordChange";
	private static final String TEXT_PASSWORD_EXPIRY = "text.passwordExpiry";
	private static final String TEXT_LAST_SIGN_ON = "text.lastSignOn";
	private static final List<String> DEFAULT_PRINCIPAL_ATTRIBUTE_NAMES = Arrays.asList(TEXT_REALM, TEXT_PRINCIPAL_NAME, TEXT_NAME, TEXT_EMAIL, TEXT_OU, TEXT_PRIMARY_EMAIL,
			TEXT_DESCRIPTION, TEXT_UUID, TEXT_CREATE_DATE, TEXT_MODIFIED_DATE, TEXT_EXPIRES, TEXT_STATUS, TEXT_LAST_PASSWORD_CHANGE, TEXT_LAST_SIGN_ON, TEXT_PASSWORD_EXPIRY);

	static Logger log = LoggerFactory.getLogger(RealmServiceImpl.class);

	private Map<String, RealmProvider> providersByModule = new HashMap<String, RealmProvider>();

	private List<RealmListener> realmListeners = new ArrayList<RealmListener>();
	private List<PrincipalProcessor> principalProcessors = new ArrayList<PrincipalProcessor>();

	@Autowired
	private RealmRepository realmRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private UpgradeService upgradeService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserVariableReplacementService userVariableReplacement;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private UserAttributeService userAttributeService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private PrincipalSuspensionRepository suspensionRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private PrincipalRepository principalRepository;

	@Autowired
	private MessageResourceService messageService;

	@Autowired
	private OrganizationalUnitRepository ouRepository;

	@Autowired
	private PasswordPolicyResourceService passwordPolicyService;

	@Autowired
	private ExportService exportService;

	@Autowired
	private I18NService i18nService;
	
	@Autowired
	private UserEnabledFilter userEnabledFilter;
	
	@Autowired
	private UserDisableFilter userDisabledFilter;
	
	@Autowired
	private UserDelegationResourceService delegationService;

	@Autowired
	private LocalAccountFilter localAccountFilter;
	
	@Autowired
	private RemoteAccountFilter remoteAccountFilter;
	
	private List<RealmOwnershipResolver> ownershipResolvers = new ArrayList<RealmOwnershipResolver>();
	private Principal systemPrincipal;
	private Realm systemRealm;
	private Cache<String, Object> realmCache;

	public static final String MESSAGE_NEW_USER_NEW_PASSWORD = "realmService.newUserNewPassword";
	public static final String MESSAGE_NEW_USER_TMP_PASSWORD = "realmService.newUserTmpPassword";
	public static final String MESSAGE_NEW_USER_SELF_CREATED = "realmService.newUserSelfCreated";
	public static final String MESSAGE_PASSWORD_CHANGED = "realmService.passwordChanged";
	public static final String MESSAGE_PASSWORD_RESET = "realmService.passwordReset";

	private static final int PAGE_SIZE = 1024;

	Map<String, TableFilter> principalFilters = new HashMap<String, TableFilter>();
	Map<String, TableFilter> builtInPrincipalFilters = new HashMap<String, TableFilter>();

	private Collection<Principal> passwordOperations = Collections.synchronizedCollection(new HashSet<>());
	
	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.realms");

		for (RealmPermission p : RealmPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.acl");

		for (UserPermission p : UserPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (ProfilePermission p : ProfilePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (GroupPermission p : GroupPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		for (RolePermission p : RolePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.password");

		for (PasswordPermission p : PasswordPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		eventService.registerEvent(RealmEvent.class, RESOURCE_BUNDLE, new RealmPropertyCollector());
		eventService.registerEvent(RealmCreatedEvent.class, RESOURCE_BUNDLE, new RealmPropertyCollector());
		eventService.registerEvent(RealmUpdatedEvent.class, RESOURCE_BUNDLE, new RealmPropertyCollector());
		eventService.registerEvent(RealmDeletedEvent.class, RESOURCE_BUNDLE, new RealmPropertyCollector());

		eventService.registerEvent(PrincipalEvent.class, RESOURCE_BUNDLE);

		eventService.registerEvent(UserEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());
		eventService.registerEvent(UserCreatedEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());
		eventService.registerEvent(UserUpdatedEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());
		eventService.registerEvent(UserDeletedEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());
		eventService.registerEvent(UserUndeletedEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());
		eventService.registerEvent(PasswordUpdateEvent.class, RESOURCE_BUNDLE, new UserPropertyCollector());

		eventService.registerEvent(GroupEvent.class, RESOURCE_BUNDLE, new GroupPropertyCollector());
		eventService.registerEvent(GroupCreatedEvent.class, RESOURCE_BUNDLE, new GroupPropertyCollector());
		eventService.registerEvent(GroupUpdatedEvent.class, RESOURCE_BUNDLE, new GroupPropertyCollector());
		eventService.registerEvent(GroupDeletedEvent.class, RESOURCE_BUNDLE, new GroupPropertyCollector());

		eventService.registerEvent(ProfileUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ChangePasswordEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ExternalPasswordEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(SetPasswordEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(ResetPasswordEvent.class, RESOURCE_BUNDLE);

		upgradeService.registerListener(this);

		realmCache = cacheService.getCacheOrCreate("realmCache", String.class, Object.class);

		EntityResourcePropertyStore.registerResourceService(Principal.class, principalRepository);
		EntityResourcePropertyStore.registerResourceService(Realm.class, realmRepository);

		messageService.registerI18nMessage(RESOURCE_BUNDLE, "realmService.newUserNewPassword",
				PrincipalWithPasswordResolver.getVariables());

		messageService.registerI18nMessage(RESOURCE_BUNDLE, "realmService.newUserTmpPassword",
				PrincipalWithPasswordResolver.getVariables());

		messageService.registerI18nMessage(RESOURCE_BUNDLE, "realmService.newUserSelfCreated",
				PrincipalWithPasswordResolver.getVariables());

		messageService.registerI18nMessage(RESOURCE_BUNDLE, "realmService.passwordChanged",
				PrincipalWithPasswordResolver.getVariables());

		messageService.registerI18nMessage(RESOURCE_BUNDLE, "realmService.passwordReset",
				PrincipalWithPasswordResolver.getVariables());

		registerBuiltInPrincipalFilter(localAccountFilter);
		registerBuiltInPrincipalFilter(remoteAccountFilter);
		registerPrincipalFilter(userEnabledFilter);
		registerPrincipalFilter(userDisabledFilter);

	}

	@Override
	public void registerPrincipalProcessor(PrincipalProcessor processor) {
		principalProcessors.add(processor);
	}

	private void registerBuiltInPrincipalFilter(TableFilter filter) {
		builtInPrincipalFilters.put(filter.getResourceKey(), filter);
	}

	@Override
	public void registerPrincipalFilter(TableFilter filter) {
		principalFilters.put(filter.getResourceKey(), filter);
	}

	@Override
	public void registerOwnershipResolver(RealmOwnershipResolver resolver) {
		ownershipResolvers.add(resolver);
	}

	@Override
	public void onUpgradeComplete() {

	}

	@Override
	public void onUpgradeFinished() {

		sessionService.executeInSystemContext(new Runnable() {
			public void run() {
				sortRealmListeners();
				for (Realm realm : realmRepository.allRealms()) {
					for (RealmListener listener : realmListeners) {
						if (!listener.hasCreatedDefaultResources(realm)) {
							try {
								listener.onCreateRealm(realm);
							} catch (ResourceException | AccessDeniedException e) {
								log.error("Failed to create default resources in realm", e);
							}
						}
					}
				}
			}
		});
	}

	@Override
	public List<RealmProvider> getProviders() throws AccessDeniedException {

		assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.READ,
				UserPermission.READ, SystemPermission.SWITCH_REALM);
		List<RealmProvider> providers = new ArrayList<>();
		for (RealmProvider p : providersByModule.values()) {
			if (p.isEnabled()) {
				providers.add(p);
			}
		}
		return providers;
	}

	@Override
	public RealmProvider getProviderForRealm(Realm realm) {
		return getProviderForRealm(realm.getResourceCategory());
	}

	@Override
	public RealmProvider getProviderForRealm(String module) {
		if (!providersByModule.containsKey(module))
			throw new IllegalArgumentException("No provider available for realm module " + module);
		return providersByModule.get(module);
	}

	protected RealmProvider getProviderForPrincipal(Principal principal) {
		if (principal instanceof LocalUser || principal instanceof FakePrincipal) {
			return getLocalProvider();
		}
		return getProviderForRealm(principal.getRealm());
	}

	protected boolean hasProviderForRealm(Realm realm) {
		return providersByModule.containsKey(realm.getResourceCategory());
	}

	@Override
	public Iterator<Principal> iterateUsers(Realm realm) {
		return iterateAllPrincipals(realm, PrincipalType.USER);
	}

	@Override
	public Iterator<Principal> iterateGroups(Realm realm) throws AccessDeniedException {
		assertAnyPermission(UserPermission.READ, GroupPermission.READ, RealmPermission.READ);
		return iterateAllPrincipals(realm, PrincipalType.GROUP);
	}

	@Override
	@Transactional
	public Set<Principal> getUsers(Realm realm, int max) {
		return getAllPrincipals(realm, max, PrincipalType.USER);
	}

	@Override
	public Set<Principal> getGroups(Realm realm, int max) throws AccessDeniedException {
		assertAnyPermission(UserPermission.READ, GroupPermission.READ, RealmPermission.READ);
		return getAllPrincipals(realm, max, PrincipalType.GROUP);
	}

	protected Set<Principal> getAllPrincipals(Realm realm, int max, PrincipalType... types) {
		Set<Principal> s = new HashSet<>();
		Iterator<Principal> p = iterateAllPrincipals(realm, types);
		while (p.hasNext())
			s.add(p.next());
		return s;
	}

	protected Iterator<Principal> iterateAllPrincipals(Realm realm, PrincipalType... types) {
		if (types.length == 0) {
			types = PrincipalType.ALL_TYPES;
		}
		return getProviderForRealm(realm).iterateAllPrincipals(realm, types);
	}

	@Override
	public void registerRealmProvider(RealmProvider provider) {

		if (log.isInfoEnabled()) {
			log.info("Registering " + provider.getModule() + " realm provider");
		}
		providersByModule.put(provider.getModule(), provider);
	}

	@Override
	public void unregisterRealmProvider(RealmProvider provider) {

		if (log.isInfoEnabled()) {
			log.info("Unregistering " + provider.getModule() + " realm provider");
		}
		providersByModule.remove(provider.getModule());
	}

	@Override
	public Realm getRealmByName(String realm) {
		return realmRepository.getRealmByName(realm);
	}

	@Override
	public Realm getRealmByNameAndOwner(String realm, Realm owner) {
		return realmRepository.getRealmByNameAndOwner(realm, owner);
	}

	@Override
	public boolean isRegistered(RealmProvider provider) {
		return providersByModule.containsKey(provider.getModule());
	}

	@Override
	public String[] getRealmPropertyArray(Realm realm, String resourceKey) {
		String value = getRealmProperty(realm, resourceKey);
		if (StringUtils.isNotBlank(value)) {
			return value.split("\\]\\|\\[");
		} else {
			return new String[0];
		}
	}

	@Override
	public String getRealmProperty(Realm realm, String resourceKey) {

		RealmProvider provider = getProviderForRealm(realm);
		return provider.getValue(realm, resourceKey);
	}

	@Override
	public String getDecryptedValue(Realm realm, String resourceKey) {

		RealmProvider provider = getProviderForRealm(realm);
		return provider.getDecryptedValue(realm, resourceKey);
	}

	@Override
	public int getRealmPropertyInt(Realm realm, String resourceKey) {
		return Integer.parseInt(getRealmProperty(realm, resourceKey));
	}

	@Override
	public boolean getRealmPropertyBoolean(Realm realm, String resourceKey) {
		return Boolean.parseBoolean(getRealmProperty(realm, resourceKey));
	}

	@Override
	public Realm getRealmByHost(String host) {
		return getRealmByHost(host, getDefaultRealm());
	}

	@Override
	public Realm getRealmByHost(String host, Realm defaultRealm) {

		if (StringUtils.isBlank(host)) {
			return defaultRealm;
		}

		if (!realmCache.containsKey(host)) {
			Set<String> hosts = new HashSet<String>();
			hosts.add(host);
			int idx;
			if ((idx = host.indexOf(":")) > -1) {
				hosts.add(host.substring(0, idx));
			}
			for (Realm r : internalAllRealms()) {
				RealmProvider provider = getProviderForRealm(r);
				String[] realmHosts = provider.getValues(r, "realm.host");
				for (String realmHost : realmHosts) {
					if (realmHost != null && !"".equals(realmHost)) {
						if (hosts.contains(realmHost)) {
							realmCache.put(host, r);

							if (log.isDebugEnabled()) {
								log.debug(String.format("Returning resolved value for host %s realm %s", host,
										r.getName()));
							}
							return r;
						}
					}
				}
			}
			return defaultRealm;
		}

		Realm realm = (Realm) realmCache.get(host);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Returning cached value for host %s realm %s", host, realm.getName()));
		}
		return realm;
	}

	@Override
	public String getRealmHostname(Realm realm) {
		RealmProvider provder = getProviderForRealm(realm);
		String[] names = provder.getValues(realm, "realm.host");
		if (names.length > 0) {
			return names[0];
		}
		return "";
	}

	@Override
	public Realm getRealmById(Long id) {

		Realm realm = realmRepository.getRealmById(id);

		return realm;
	}

	@Override
	public Realm getRealmByOwner(Long owner) throws AccessDeniedException {

		assertAnyPermission(RealmPermission.READ, SystemPermission.SWITCH_REALM);

		return realmRepository.getRealmByOwner(owner);
	}

	@Override
	public Map<String, String> filterSecretProperties(Principal principal, RealmProvider provider,
			Map<String, String> properties) {

		for (PropertyTemplate template : provider.getPropertyTemplates(principal)) {
			if (properties.containsKey(template.getResourceKey()) && template.isEncrypted()) {
				properties.put(template.getResourceKey(), "**********");
			}
		}

		if (principal != null) {
			for (UserAttribute attr : userAttributeService.getPersonalResources(principal)) {
				if (properties.containsKey(attr.getVariableName())) {
					if (attr.getEncrypted() || attr.getType() == AttributeType.PASSWORD) {
						properties.put(attr.getVariableName(), "**********");
					}
				}
			}
		}
		return properties;
	}

	@Override
	public Principal createLocalUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, String password, boolean forceChange, boolean selfCreated,
			boolean sendNotifications) throws ResourceException, AccessDeniedException {

		RealmProvider provider = getLocalProvider();

		return createUser(realm, username, properties, principals, password, forceChange, selfCreated, null, provider,
				sendNotifications);
	}

	@Override
	public Principal createLocalUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated,
			boolean sendNotifications, PrincipalType type) throws ResourceException, AccessDeniedException {

		RealmProvider provider = getLocalProvider();

		return createUser(realm, username, properties, principals, passwordCreator, forceChange, selfCreated, null,
				provider, sendNotifications, type);
	}

	@Override
	public RealmProvider getLocalProvider() {
		return getProviderForRealm(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY);
	}

	@Override
	public Principal createUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, String password, boolean forceChange, boolean selfCreated,
			boolean sendNotifications) throws ResourceException, AccessDeniedException {
		return createUser(realm, username, properties, principals, password, forceChange, selfCreated, (Principal)null,
				getProviderForRealm(realm), sendNotifications);
	}

	public Principal createUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, final String password, boolean forceChange, boolean selfCreated,
			Principal parent, RealmProvider provider, boolean sendNotifications)
			throws ResourceException, AccessDeniedException {
		return createUser(realm, username, properties, principals, new DefaultPasswordCreator(password), forceChange,
				selfCreated, parent, provider, sendNotifications);
	}

	@Override
	public Principal createUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, PasswordCreator password, boolean forceChange, boolean selfCreated,
			boolean sendNotifications) throws ResourceException, AccessDeniedException {
		return createUser(realm, username, properties, principals, password, forceChange, selfCreated, null,
				getProviderForRealm(realm), sendNotifications);
	}

	@Override
	public Principal createUser(Realm realm, String username, Map<String, String> properties,
			List<Principal> principals, PasswordCreator passwordCreator, boolean forceChange, boolean selfCreated,
			Principal parent, RealmProvider provider, boolean sendNotifications, PrincipalType type)
			throws ResourceException, AccessDeniedException {

		try {

			assertAnyPermission(UserPermission.CREATE, RealmPermission.CREATE);

			if("true".equalsIgnoreCase(properties.get("createLocalAccount"))) {
				provider = getLocalProvider();
			}
			
			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}

			Principal existing = getPrincipalByName(realm, username, PrincipalType.USER, PrincipalType.SYSTEM, PrincipalType.USER);
			if (existing != null) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.principalAlreadyExists", username);
			}

			for (PrincipalProcessor processor : principalProcessors) {
				processor.beforeCreate(realm, provider.getModule(), username, properties);
			}
 
			Principal principal = provider.createUser(realm, username, properties, principals, passwordCreator,
					forceChange, type);

			for (PrincipalProcessor processor : principalProcessors) {
				processor.afterCreate(principal, passwordCreator.getPassword(), properties);
			}

			provider.reconcileUser(principal);
			log.info(String.format("Created user %s in realm %s", principal.getName(), realm.getName()));
			eventService.publishEvent(new UserCreatedEvent(this, getCurrentSession(), realm, provider, principal,
					principals, filterSecretProperties(principal, provider, properties), passwordCreator.getPassword(),
					forceChange, selfCreated));

			if (sendNotifications) {
				transactionService.doInTransaction((t) -> {
					try {
						if (StringUtils.isNotBlank(principal.getEmail())) {
							if (selfCreated) {
								sendNewUserSelfCreatedNofification(principal, passwordCreator.getPassword());
							} else if (forceChange) {
								sendNewUserTemporaryPasswordNofification(principal, passwordCreator.getPassword());
							} else {
								sendNewUserFixedPasswordNotification(principal, passwordCreator.getPassword());
							}
						}
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException("Failed send notifications.", e);
					}
					return null;
				});
			}

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserCreatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(null, provider, properties), principals));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(new UserCreatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(null, provider, properties), principals));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(new UserCreatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(null, provider, properties), principals));
			throw new ResourceCreationException(e, RESOURCE_BUNDLE, "error.unexpectedError", e.getMessage());
		}

	}

	private void sendNewUserTemporaryPasswordNofification(Principal principal, String password)
			throws ResourceException, AccessDeniedException {
		PrincipalWithPasswordResolver resolver = new PrincipalWithPasswordResolver((UserPrincipal<?>) principal, password,
				true);
		messageService.sendMessage(MESSAGE_NEW_USER_TMP_PASSWORD, principal.getRealm(), resolver, principal);
	}

	private void sendNewUserSelfCreatedNofification(Principal principal, String password)
			throws ResourceException, AccessDeniedException {
		PrincipalWithPasswordResolver resolver = new PrincipalWithPasswordResolver((UserPrincipal<?>) principal, password,
				false);
		messageService.sendMessage(MESSAGE_NEW_USER_SELF_CREATED, principal.getRealm(), resolver, principal);
	}

	private void sendNewUserFixedPasswordNotification(Principal principal, String password)
			throws ResourceException, AccessDeniedException {
		PrincipalWithPasswordResolver resolver = new PrincipalWithPasswordResolver((UserPrincipal<?>) principal, password,
				false);
		messageService.sendMessage(MESSAGE_NEW_USER_NEW_PASSWORD, principal.getRealm(), resolver, principal);
	}

	@Override
	public Principal updateUserProperties(Principal user, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

		final RealmProvider provider = getProviderForPrincipal(user);
		final Principal existing = getPrincipalById(user.getId());
		Map<String, String> oldProperties = getUserPropertyValues(existing);
		List<Principal> associated = getAssociatedPrincipals(existing);
		try {

			assertAnyPermission(ProfilePermission.UPDATE, UserPermission.UPDATE, RealmPermission.UPDATE);

			try {
				delegationService.assertDelegation(user);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			for (PrincipalProcessor processor : principalProcessors) {
				processor.beforeUpdate(user, properties);
			}

			Principal principal = provider.updateUserProperties(user, properties);

			for (PrincipalProcessor processor : principalProcessors) {
				processor.afterUpdate(principal, properties);
			}

			log.info(String.format("Updated properties user %s in realm %s", principal.getName(), principal.getRealm().getName()));
			eventService.publishEvent(
					new UserUpdatedEvent(this, getCurrentSession(), principal.getRealm(), provider, principal,
							getAssociatedPrincipals(principal), filterSecretProperties(principal, provider, properties),
							associated, filterSecretProperties(principal, provider, oldProperties)));

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), user.getRealm(), provider,
					user.getPrincipalName(), filterSecretProperties(user, provider, properties), associated));
			throw e;
		} catch (ResourceException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), user.getRealm(), provider,
					user.getPrincipalName(), filterSecretProperties(user, provider, properties), associated));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), user.getRealm(), provider,
					user.getPrincipalName(), filterSecretProperties(user, provider, properties), associated));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "updateUser.unexpectedError", e.getMessage());
		}
	}

	@Override
	public Principal updateUser(Realm realm, Principal user, String username, Map<String, String> properties,
			List<Principal> principals) throws ResourceException, AccessDeniedException {

		final RealmProvider provider = getProviderForPrincipal(user);
		Principal existing = getPrincipalById(user.getId());
		Map<String, String> oldProperties = getUserPropertyValues(existing);
		List<Principal> previousPrincipals = getAssociatedPrincipals(existing);

		try {

			assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

			try {
				delegationService.assertDelegation(user);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			for (PrincipalProcessor processor : principalProcessors) {
				processor.beforeUpdate(user, properties);
			}

			Principal principal = provider.updateUser(realm, user, username, properties, principals);

			for (PrincipalProcessor processor : principalProcessors) {
				processor.afterUpdate(principal, properties);
			}

			log.info(String.format("Updated user %s in realm %s", principal.getName(), principal.getRealm().getName()));
			eventService.publishEvent(new UserUpdatedEvent(this, getCurrentSession(), realm, provider, principal,
					getAssociatedPrincipals(principal), filterSecretProperties(principal, provider, properties),
					previousPrincipals, filterSecretProperties(principal, provider, oldProperties)));

			return principal;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(user, provider, properties), principals));
			throw e;
		} catch (ResourceException e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(user, provider, properties), principals));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(new UserUpdatedEvent(this, e, getCurrentSession(), realm, provider, username,
					filterSecretProperties(user, provider, properties), principals));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "updateUser.unexpectedError", e.getMessage());
		}
	}

	@Override
	public boolean verifyPrincipal(String principalName, Realm realm) {

		Collection<PrincipalSuspension> suspensions = suspensionRepository.getSuspensions(principalName, realm);

		for (PrincipalSuspension s : suspensions) {
			if (s.isActive()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean verifyPassword(Principal principal, char[] password) throws LogonException, IOException {

		/**
		 * Adds support for session tokens. These can be created and used instead of
		 * passwords where we may not have, or want to distribute the password to an
		 * external service.
		 */

		setupSystemContext(principal.getRealm());
		try {
			String pwd = new String(password);
			if (pwd.startsWith(SessionServiceImpl.TOKEN_PREFIX)) {
				Principal tokenPrincipal = sessionService.getSessionTokenResource(pwd, Principal.class);
				return tokenPrincipal != null && tokenPrincipal.equals(principal);
			} else {
				return getProviderForPrincipal(principal).verifyPassword(principal, password);
			}
		} catch (LogonException e) {
			if (configurationService.getBooleanValue(principal.getRealm(), "logon.verboseErrors")) {
				throw e;
			}
			return false;
		} finally {
			clearPrincipalContext();
		}
	}

	@Override
	public Principal getPrincipalByName(Realm realm, String principalName, PrincipalType... type) {
		if (type.length == 0) {
			type = PrincipalType.ALL_TYPES;
		}
		if (realm == null) {
			try {
				return getUniquePrincipal(principalName, type);
			} catch (ResourceNotFoundException e) {
				return null;
			}
		} else {
			try {
				return getUniquePrincipalForRealm(principalName, realm, type);
			} catch (ResourceNotFoundException e) {
				return null;
			}
		}
	}

	@Override
	public void deleteRealm(String name) throws ResourceException, ResourceNotFoundException, AccessDeniedException {

		assertPermission(RealmPermission.DELETE);

		Realm realm = getRealmByName(name);

		if (realm == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.invalidRealm", name);
		}

		deleteRealm(realm);
	}

	@Override
	public List<Realm> allRealms() throws AccessDeniedException {
		return filterRealms(null, false);
	}

	@Override
	public List<Realm> allRealms(boolean ignoreMissingProvider) {
		return filterRealms(null, ignoreMissingProvider);
	}

	private List<Realm> internalAllRealms() {
		return filterRealms(null, false);
	}

	private List<Realm> filterRealms(Class<? extends RealmProvider> clz, boolean ignoreMissingProvider) {

		List<Realm> realms = realmRepository.allRealms();
		List<Realm> ret = new ArrayList<Realm>(realms);
		for (Realm r : realms) {
			if (!ignoreMissingProvider) {
				if (!hasProviderForRealm(r)) {
					ret.remove(r);
					continue;
				}
				if (clz != null && !clz.isAssignableFrom(getProviderForRealm(r).getClass())) {
					ret.remove(r);
				}
			}
		}
		return ret;
	}

	@Override
	public List<Realm> allRealms(Class<? extends RealmProvider> clz) {
		return filterRealms(clz, false);
	}

	@Override
	public void changePassword(final Principal principal, final String oldPassword, final String newPassword)
			throws ResourceException, AccessDeniedException {

		assertPermission(PasswordPermission.CHANGE);
		final RealmProvider provider = getProviderForPrincipal(principal);

		transactionService.doInTransaction(new TransactionCallbackWithError<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus status) {

				try {
					
					passwordOperations.add(principal);
					
					for (PrincipalProcessor proc : principalProcessors) {
						proc.beforeChangePassword(principal, newPassword, oldPassword);
					}

					if (!verifyPassword(principal, oldPassword.toCharArray())) {
						throw new ResourceChangeException(RESOURCE_BUNDLE, "error.invalidPassword");
					}

					provider.changePassword(principal, oldPassword.toCharArray(), newPassword.toCharArray());

					setCurrentPassword(newPassword);

					for (PrincipalProcessor proc : principalProcessors) {
						proc.afterChangePassword(principal, newPassword, oldPassword);
					}

					eventService.publishEvent(new ChangePasswordEvent(this, getCurrentSession(), 
							getCurrentRealm(), provider, newPassword, principal));

					messageService.sendMessageNow(MESSAGE_PASSWORD_CHANGED, principal.getRealm(),
							new PrincipalWithoutPasswordResolver((UserPrincipal<?>) principal), Arrays.asList(principal));
					
					return principal;
				} catch (Throwable t) {
					throw new IllegalStateException(t.getMessage(), t);
				} finally {
					passwordOperations.remove(principal);
				}
			}

			@Override
			public void doTransacationError(Throwable t) {
				eventService.publishEvent(new ChangePasswordEvent(this, 
						t, getCurrentSession(), getCurrentRealm(),
						provider, newPassword, principal));
			}
		});
	}

	@Override
	public void setPassword(Principal principal, String password, boolean forceChangeAtNextLogon,
			boolean administrative) throws ResourceException, AccessDeniedException {

		if (permissionService.hasSystemPermission(principal)) {
			try {
				assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.sysadminOnly");
			}
		} else if (!getCurrentPrincipal().equals(principal)) {
			try {
				assertAnyPermission(UserPermission.CREATE, UserPermission.UPDATE, PasswordPermission.RESET);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noUserChangePermission");
			}
			
			try {
				delegationService.assertDelegation(principal);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
		}

		RealmProvider provider = getProviderForPrincipal(principal);

		try {

			passwordOperations.add(principal);
			
			for (PrincipalProcessor proc : principalProcessors) {
				proc.beforeSetPassword(principal, password);
			}

			provider.setPassword(principal, password.toCharArray(), forceChangeAtNextLogon, administrative);

			for (PrincipalProcessor proc : principalProcessors) {
				proc.afterSetPassword(principal, password);
			}
			
			if (administrative) {

				eventService.publishEvent(new SetPasswordEvent(this, getCurrentSession(), getCurrentRealm(), provider,
						principal, password));

				messageService.sendMessageNow(MESSAGE_PASSWORD_RESET, principal.getRealm(),
						transactionService.doInTransaction((status) -> 
							new PrincipalWithPasswordResolver((UserPrincipal<?>) principal, password, forceChangeAtNextLogon)
								),
						Arrays.asList(principal));
			} else {

				eventService.publishEvent(new ResetPasswordEvent(this, getCurrentSession(), getCurrentRealm(), provider,
						principal, password));

				messageService.sendMessage(MESSAGE_PASSWORD_CHANGED, principal.getRealm(),
						new PrincipalWithoutPasswordResolver((UserPrincipal<?>) principal), Arrays.asList(principal));
			}

		} catch (ResourceException ex) {
			if (administrative) {
				eventService.publishEvent(new SetPasswordEvent(this, ex, getCurrentSession(), getCurrentRealm(),
						provider, principal.getPrincipalName(), password));
			} else {
				eventService.publishEvent(new ResetPasswordEvent(this, ex, getCurrentSession(), getCurrentRealm(),
						provider, principal.getPrincipalName(), password));
			}
			throw ex;
		} finally {
			passwordOperations.remove(principal);
		}

	}

	@Override
	public boolean isChangingPassword(Principal principal) {
		return passwordOperations.contains(principal);
	}
	
	@Override
	public boolean isReadOnly(Realm realm) {

		RealmProvider provider = getProviderForRealm(realm);
		return provider.isReadOnly(realm);
	}

	@Override
	public Realm getSystemRealm() {
		if (systemRealm == null) {
			systemRealm = realmRepository.getSystemRealm();
		}
		return systemRealm;
	}

	@Override
	public Principal getSystemPrincipal() {
		if (systemPrincipal == null) {
			systemPrincipal = getPrincipalByName(realmRepository.getSystemRealm(), 
					SYSTEM_PRINCIPAL,
					PrincipalType.SYSTEM);
			if(systemPrincipal == null) {
				throw new IllegalStateException("Could not get system principal. This may happen on "
						+ "fresh installs, if getSystemPrincipal() is called before the system user is "
						+ "created in the upgrade scripts. Make sure no service (e.g. in a @PostConstruct) is "
						+ "attempting to obtain the system principal.");
			}
		}
		return systemPrincipal;
	}

	@Override
	public Realm createPrimaryRealm(String name, String module, Map<String, String> properties)
			throws AccessDeniedException, ResourceException {
		return createRealm(name, module, 
				getCurrentRealm().isSystem() ? null : getCurrentRealm(),  
					null, properties);
	}

	@Override
	public Realm createRealm(String name, String module, Realm parent, Long owner, Map<String, String> properties)
			throws AccessDeniedException, ResourceException {

		try {
			Principal originalPrincipal = getCurrentPrincipal();
			assertPermission(RealmPermission.CREATE);

			if (realmRepository.getRealmByName(name) != null) {
				ResourceCreationException ex = new ResourceCreationException(RESOURCE_BUNDLE, "error.nameAlreadyExists",
						name);
				eventService.publishEvent(new RealmCreatedEvent(this, ex, getCurrentSession(), name, module));
				throw ex;
			}

			final RealmProvider realmProvider = getProviderForRealm(module);

			realmProvider.assertCreateRealm(properties);

			Map<String, String> allDefaultProperties = realmProvider.getProperties(null);
			allDefaultProperties.putAll(properties);
			realmProvider.testConnection(allDefaultProperties, (updatedProperties) -> {
				/* This is called if testing is interrupted to
				 * an oauth authorization endpoint. When we eventually
				 * get authorization, we want to update the realm with
				 * the new authorized details (this is currently being used
				 * to create service accounts for LogonBox directory connector).
				 */
				setupSystemContext(originalPrincipal);
				allDefaultProperties.putAll(updatedProperties);
				try {
					createRealm(name, module, parent, owner, allDefaultProperties);
				}
				finally {
					clearPrincipalContext();
				}
			});

			@SuppressWarnings("unchecked")
			Realm realm = realmRepository.createRealm(name, UUID.randomUUID().toString(), module, allDefaultProperties,
					realmProvider, parent, owner, owner == null, new TransactionAdapter<Realm>() {

						@Override
						public void afterOperation(Realm realm, Map<String, String> properties) {
							try {
								configurationService.setValue(realm, "realm.userEditableProperties", "");
								configurationService.setValue(realm, "realm.userVisibleProperties",
										ResourceUtils.implodeValues(realmProvider.getDefaultUserPropertyNames()));

								realm.setReadOnly(realmProvider.isReadOnly(realm));
								realmRepository.saveRealm(realm);

								String externalHost = getRealmHostname(realm);
								String currentExternalHost = configurationService.getValue(realm,
										"email.externalHostname");
								if (StringUtils.isNotBlank(externalHost) && (StringUtils.isBlank(currentExternalHost)
										|| !currentExternalHost.equals(externalHost))) {
									configurationService.setValue(realm, "email.externalHostname", externalHost);
								}
								fireRealmCreate(realm);

							} catch (Throwable e) {
								throw new IllegalStateException(e.getMessage(), e);
							}
						}
					});

			eventService.publishEvent(new RealmCreatedEvent(this, getCurrentSession(), realm));

			return realm;
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmCreatedEvent(this, e, getCurrentSession(), name, module));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(new RealmCreatedEvent(this, e, getCurrentSession(), name, module));
			throw e;
		} catch (ResourcePassthroughException e) {
			throw e;
		} catch (Throwable t) {
			eventService.publishEvent(new RealmCreatedEvent(this, t, getCurrentSession(), name, module));
			throw new ResourceCreationException(t, RESOURCE_BUNDLE, "error.genericError", name, t.getMessage());
		}
	}

	private void clearCache(Realm realm) {
		RealmProvider realmProvider = getProviderForRealm(realm.getResourceCategory());

		String[] hosts = realmProvider.getValues(realm, "realm.host");
		for (String host : hosts) {
			realmCache.remove(host);
		}
	}

	@Override
	public void setRealmProperty(Realm realm, String resourceKey, String value) throws AccessDeniedException {

		assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.UPDATE);
		RealmProvider realmProvider = getProviderForRealm(realm.getResourceCategory());

		realmProvider.setValue(realm, resourceKey, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Realm resetRealm(Realm realm) throws ResourceException, AccessDeniedException {

		try {

			assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.UPDATE);

			String resourceCategory = realm.getResourceCategory();
			final RealmProvider oldProvider = getProviderForRealm(realm.getResourceCategory());

			final boolean changedType = !resourceCategory.equals(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY);

			if (!changedType) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmCannotBeReset");
			}

			final RealmProvider realmProvider = getProviderForRealm(realm.getResourceCategory());

			realm.setResourceCategory(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY);

			clearCache(realm);

			realm = realmRepository.saveRealm(realm, new HashMap<String, String>(), getProviderForRealm(realm),
					new TransactionAdapter<Realm>() {

						@Override
						public void afterOperation(Realm realm, Map<String, String> properties) {
							try {

								oldProvider.resetRealm(realm);
								realmProvider.resetRealm(realm);

								configurationService.setValue(realm, "realm.userEditableProperties", "");
								configurationService.setValue(realm, "realm.userVisibleProperties",
										ResourceUtils.implodeValues(realmProvider.getDefaultUserPropertyNames()));

								realm.setReadOnly(false);
								realmRepository.saveRealm(realm);

								fireRealmUpdate(realm);

							} catch (Throwable e) {
								throw new IllegalStateException(e.getMessage(), e);
							}
						}
					});

			eventService.publishEvent(new RealmUpdatedEvent(this, getCurrentSession(), realm.getName(),
					realmRepository.getRealmById(realm.getId())));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (ResourcePassthroughException e) {
			throw e;
		} catch (Throwable t) {
			log.error("Unexpected error", t);
			eventService.publishEvent(new RealmUpdatedEvent(this, t, getCurrentSession(), realm));
			throw new ResourceChangeException(t, RESOURCE_BUNDLE, "error.unexpectedError", t.getMessage());
		}
		return realm;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Realm updateRealm(final Realm realm, String name, String type, Map<String, String> properties)
			throws AccessDeniedException, ResourceException {

		try {

			Principal originalPrincipal = getCurrentPrincipal();
			assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.UPDATE);
			
			String propertyChangesCSV = computePropertyChanges(realm, properties);

			if (!realm.getName().equalsIgnoreCase(name)) {
				if (realmRepository.getRealmByName(name) != null) {
					throw new ResourceChangeException(RESOURCE_BUNDLE, "error.nameAlreadyExists", name);
				}
			}

			String resourceCategory = realm.getResourceCategory();
			final boolean changedType = !resourceCategory.equals(type);

			realm.setResourceCategory(type);
			final RealmProvider realmProvider = getProviderForRealm(realm.getResourceCategory());

			/**
			 * Switch to system context in the updated realm so that updates from system
			 * realm will be be able to correctly route through a secure node.
			 */
			setupSystemContext(realm);
			try {
				Map<String, String> existingProperties = getRealmProperties(realm);
				
				boolean reconfigured = false;
				for (PropertyCategory cat : getRealmPropertyTemplates(realm)) {
					for (AbstractPropertyTemplate t : cat.getTemplates()) {
						if (t.getAttributes().containsKey("reconfigure")
								&& "true".equalsIgnoreCase(t.getAttributes().get("reconfigure"))) {
							if (properties.containsKey(t.getResourceKey()) && !Objects.equals(properties.get(t.getResourceKey()), existingProperties.get(t.getResourceKey()))) {
								reconfigured = true;
								break;
							}
						}
					}
					if (reconfigured) {
						break;
					}
				}
				existingProperties.putAll(properties);
				realmProvider.testConnection(existingProperties, realm, (updatedProperties) -> {
					
					/* This is called if testing is interrupted to
					 * an oauth authorization endpoint. When we eventually
					 * get authorization, we want to update the realm with
					 * the new authorized details (this is currently being used
					 * to create service accounts for LogonBox directory connector).
					 */
					setupSystemContext(originalPrincipal);
					existingProperties.putAll(updatedProperties);
					try {
						updateRealm(realm, name, type, existingProperties);
					}
					finally {
						clearPrincipalContext();
					}
				});

				if (reconfigured) {
					log.info("A realm property that was tagged with 'reconfigure' has been changed, the next sync. will rebuild the cache and retrieve all filtered objects.");
					realmProvider.setValue(realm, "realm.reconcileRebuildCache", "true");
					realmProvider.setValue(realm, "realm.upToDate", "false");
				}
			} finally {
				clearPrincipalContext();
			}

			String oldName = realm.getName();

			clearCache(realm);

			realm.setName(name);

			Realm newRealm = realmRepository.saveRealm(realm, properties, getProviderForRealm(realm),
					new TransactionAdapter<Realm>() {

						@Override
						public void afterOperation(Realm realm, Map<String, String> properties) {
							try {

								if (changedType) {
									configurationService.setValue(realm, "realm.userEditableProperties",
											ResourceUtils.implodeValues(realmProvider.getDefaultUserPropertyNames()));

									realm.setReadOnly(realmProvider.isReadOnly(realm));
									realmRepository.saveRealm(realm);
								}

								String externalHost = getRealmHostname(realm);
								if (StringUtils.isNotBlank(externalHost)) {
									configurationService.setValue(realm, "email.externalHostname", externalHost);
								}

								fireRealmUpdate(realm);

							} catch (Throwable e) {
								throw new IllegalStateException(e.getMessage(), e);
							}
						}
					});

			eventService.publishEvent(new RealmUpdatedEvent(this, getCurrentSession(), oldName,
					realmRepository.getRealmById(realm.getId()), propertyChangesCSV));

			return newRealm;

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new RealmUpdatedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (ResourcePassthroughException e) {
			/* Let these bubble up with no events, nothing has changed */
			throw e;
		} catch (Throwable t) {
			log.error("Unexpected error", t);
			eventService.publishEvent(new RealmUpdatedEvent(this, t, getCurrentSession(), realm));
			throw new ResourceChangeException(t, RESOURCE_BUNDLE, "error.unexpectedError", t.getMessage());
		}
	}

	private void fireRealmUpdate(Realm realm) throws ResourceException {

		for (RealmListener l : realmListeners) {
			try {
				l.onUpdateRealm(realm);
			} catch (ResourceChangeException e) {
				throw e;
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	private void fireRealmCreate(Realm realm) throws ResourceException {

		sortRealmListeners();

		for (RealmListener l : realmListeners) {
			try {
				l.onCreateRealm(realm);
			} catch (ResourceCreationException e) {
				throw e;
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	protected void sortRealmListeners() {
		Collections.<RealmListener>sort(realmListeners, new Comparator<RealmListener>() {

			@Override
			public int compare(RealmListener o1, RealmListener o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
	}

	private void fireRealmDelete(Realm realm) throws ResourceException {

		for (RealmListener l : realmListeners) {
			try {
				l.onDeleteRealm(realm);
			} catch (ResourceChangeException e) {
				throw e;
			} catch (Throwable t) {
				log.error("Caught error in RealmListener", t);
			}
		}
	}

	@Override
	public void deleteRealm(final Realm realm) throws AccessDeniedException, ResourceException {

		try {

			assertPermission(RealmPermission.DELETE);

			if (realm.isDefaultRealm()) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.cannotDeleteDefault", realm.getName());
			}

			if (realm.isSystem()) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.cannotDeleteSystem", realm.getName());
			}

			eventService.delayEvents();
			try {

				transactionService.doInTransaction(new TransactionCallback<Void>() {

					@Override
					public Void doInTransaction(TransactionStatus status) {
						try {
							/**
							 * Get a copy of the realm to delete so we can fire events with the current
							 * realm detail as delete will rename it
							 */

							Realm deletedRealm = getRealmById(realm.getId());
							deletedRealm.getRoles();

							clearCache(deletedRealm);

							sessionService.deleteRealm(realm);

							fireRealmDelete(deletedRealm);

							Collection<FindableResourceRepository<?>> repos = EntityResourcePropertyStore
									.getRepositories();
							log.info(String.format("Repositories: %s", repos));
							for (FindableResourceRepository<?> repository : repos) {
								if (repository instanceof AbstractSimpleResourceRepository
										&& !(repository instanceof RealmRepository)
										&& !(repository instanceof PermissionRepository)) {
									if (repository.isDeletable()) {
										repository.deleteRealm(realm);
									}
								}
								if (repository instanceof AbstractAssignableResourceRepository) {
									AbstractAssignableResourceRepository<?> r = (AbstractAssignableResourceRepository<?>) repository;
									if (r.isDeletable()) {
										r.deleteRealm(realm);
									}
								}
							}

							permissionRepository.deleteRealm(realm);
							ouRepository.deleteRealm(realm);

							suspensionRepository.deleteRealm(realm);
							getLocalProvider().deleteRealm(realm);

							RealmProvider provider = getProviderForRealm(realm);
							provider.deleteRealm(realm);

							passwordPolicyService.deleteRealm(realm);
							configurationService.deleteRealm(realm);
							realmRepository.deleteRealmRoles(realm);
							realmRepository.deleteRealm(deletedRealm);
							return null;
						} catch (ResourceException e) {
							throw new IllegalStateException(e.getMessage(), e);
						}
					}
				});
			} finally {
				eventService.undelayEvents();
			}

			eventService.publishDelayedEvents();
			eventService.publishEvent(new RealmDeletedEvent(this, getCurrentSession(), realm));

		} catch (AccessDeniedException e) {
			eventService.rollbackDelayedEvents(false);
			eventService.publishEvent(new RealmDeletedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.rollbackDelayedEvents(false);
			eventService.publishEvent(new RealmDeletedEvent(this, e, getCurrentSession(), realm));
			throw e;
		} catch (Throwable t) {
			eventService.rollbackDelayedEvents(false);
			eventService.publishEvent(new RealmDeletedEvent(this, t, getCurrentSession(), realm));
			throw new ResourceChangeException(t, RESOURCE_BUNDLE, "error.unexpectedError");
		}
	}

	@Override
	public Realm setDefaultRealm(Realm realm) throws AccessDeniedException {
		assertPermission(SystemPermission.SYSTEM_ADMINISTRATION);

		return realmRepository.setDefaultRealm(realm);
	}

	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(Realm realm) throws AccessDeniedException {

		assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);

		return provider.getRealmProperties(realm);

	}

	@Override
	public Collection<PropertyCategory> getRealmPropertyTemplates(String module) throws AccessDeniedException {

		assertAnyPermissionOrRealmAdministrator(PermissionScope.INCLUDE_CHILD_REALMS, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getRealmProperties(null);
	}

	@Override
	@Deprecated
	public Principal getPrincipalById(Realm realm, Long id, PrincipalType... type) throws AccessDeniedException {

		Principal principal = principalRepository.getResourceById(id);
		return principal;
	}

	@Override
	public Principal getPrincipalById(Long id) {

		Principal principal = principalRepository.getResourceById(id);
		return principal;
	}

	@Override
	public Principal getDeletedPrincipalById(Realm realm, Long id, PrincipalType... type) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ, RealmPermission.READ);

		if (type.length == 0) {
			type = PrincipalType.ALL_TYPES;
		}
		Principal principal = getProviderForRealm(realm).getDeletedPrincipalById(id, realm, type);
		if (principal == null) {
			return getLocalProvider().getDeletedPrincipalById(id, realm, type);
		}
		return principal;
	}

	@Override
	public Principal getPrincipalByEmail(Realm realm, String email)
			throws AccessDeniedException, ResourceNotFoundException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ, RealmPermission.READ);

		Principal principal = getProviderForRealm(realm).getPrincipalByEmail(realm, email);
		if (principal == null) {
			principal = getLocalProvider().getPrincipalByEmail(realm, email);
		}
		if (principal == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.noPrincipalForEmail", email);
		}
		return principal;
	}

	@Override
	public UserPrincipal<?> getPrincipalByFullName(Realm realm, String fullName)
			throws AccessDeniedException, ResourceNotFoundException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ, RealmPermission.READ);

		UserPrincipal<?> principal = getProviderForRealm(realm).getPrincipalByFullName(realm, fullName);
		if (principal == null) {
			principal = getLocalProvider().getPrincipalByFullName(realm, fullName);
		}
		if (principal == null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.noPrincipalForFullName", fullName);
		}
		return principal;
	}

	@Override
	public boolean requiresPasswordChange(Principal principal, Realm realm) {
		return getProviderForPrincipal(principal).requiresPasswordChange(principal);
	}

	@Override
	public Principal createGroup(Realm realm, String name, Map<String, String> properties,
			final List<Principal> principals, final List<Principal> groups)
			throws ResourceException, AccessDeniedException {

		RealmProvider provider = getProviderForRealm(realm);

		try {
			assertAnyPermission(GroupPermission.CREATE, RealmPermission.CREATE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}

			Principal group = getPrincipalByName(realm, name, PrincipalType.GROUP);

			if (group != null) {
				ResourceCreationException ex = new ResourceCreationException(RESOURCE_BUNDLE,
						"error.group.alreadyExists", name);
				throw ex;
			}

			Principal principal = provider.createGroup(realm, name, properties, principals, groups);

			log.info(String.format("Created group %s in realm %s", principal.getName(), realm.getName()));
			eventService.publishEvent(
					new GroupCreatedEvent(this, getCurrentSession(), realm, provider, principal, principals));
			return principal;

		} catch (AccessDeniedException e) {
			eventService.publishEvent(
					new GroupCreatedEvent(this, e, getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (ResourceCreationException e) {
			eventService.publishEvent(
					new GroupCreatedEvent(this, e, getCurrentSession(), realm, provider, name, principals));
			throw e;
		} catch (Exception e) {
			eventService.publishEvent(
					new GroupCreatedEvent(this, e, getCurrentSession(), realm, provider, name, principals));
			throw new ResourceCreationException(e, RESOURCE_BUNDLE, "createGroup.unexpectedError", e.getMessage());
		}
	}

	@Override
	public Principal updateGroup(final Realm realm, final Principal group, final String name,
			final Map<String, String> properties, final List<Principal> principals, final List<Principal> groups)
			throws ResourceException, AccessDeniedException {

		final RealmProvider provider = getProviderForRealm(realm);

		assertAnyPermission(GroupPermission.UPDATE, RealmPermission.UPDATE);

		if (provider.isReadOnly(realm)) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
		}
		
		try {
			delegationService.assertDelegation(group);
			delegationService.assertDelegation(principals);
		} catch (AccessDeniedException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
		}

		Principal tmpGroup = getPrincipalByName(realm, name, PrincipalType.GROUP);

		if (tmpGroup != null && !tmpGroup.getId().equals(group.getId())) {
			ResourceChangeException ex = new ResourceChangeException(RESOURCE_BUNDLE, "error.group.alreadyExists",
					name);
			throw ex;
		}

		Collection<Principal> existingPrincipals = new HashSet<Principal>();
		existingPrincipals.addAll(getGroupUsers(group));
		existingPrincipals.addAll(getGroupGroups(group));

		final Collection<Principal> assigned = new ArrayList<Principal>();
		assigned.addAll(principals);
		assigned.addAll(groups);

		assigned.removeAll(existingPrincipals);

		final Collection<Principal> unassigned = new ArrayList<Principal>();
		unassigned.addAll(existingPrincipals);
		unassigned.removeAll(principals);
		unassigned.removeAll(groups);

		final Collection<Principal> all = new HashSet<Principal>();
		all.addAll(principals);
		all.addAll(groups);

		return transactionService.doInTransaction(new TransactionCallbackWithError<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus arg0) {

				try {
					for (PrincipalProcessor processor : principalProcessors) {
						processor.beforeUpdate(group, properties);
					}

					Principal principal = provider.updateGroup(realm, group, name, properties, principals, groups);

					for (PrincipalProcessor processor : principalProcessors) {
						processor.afterUpdate(principal, properties);
					}

					log.info(String.format("Created group %s in realm %s", principal.getName(), realm.getName()));
					eventService.publishEvent(new GroupUpdatedEvent(this, getCurrentSession(), realm, provider,
							principal, all, assigned, unassigned));

					return principal;
				} catch (ResourceChangeException e) {
					throw new IllegalStateException(e.getMessage(), e);
				} catch (Throwable e) {
					log.error("Failed to create group.", e);
					ResourceChangeException ex = new ResourceChangeException(RESOURCE_BUNDLE,
							"groupUser.unexpectedError", e.getMessage(), e);
					throw new IllegalStateException(ex.getMessage(), ex);
				}
			}

			@Override
			public void doTransacationError(Throwable e) {
				eventService.publishEvent(
						new GroupUpdatedEvent(this, e, getCurrentSession(), realm, provider, name, principals));
			}

		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteGroup(Realm realm, final Principal group, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException {

		final RealmProvider provider = getProviderForRealm(realm);

		Collection<Principal> assosiatedPrincipals = provider.getAssociatedPrincipals(group);

		try {
			assertAnyPermission(GroupPermission.DELETE, RealmPermission.DELETE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}
			
			try {
				delegationService.assertDelegation(group);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}

			permissionService.revokePermissions(group, new TransactionAdapter<Principal>() {
				@Override
				public void beforeOperation(Principal resource, Map<String, String> properties)
						throws ResourceException {
					try {
						provider.deleteGroup(group, deleteLocallyOnly);
					} catch (ResourceChangeException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
			});

			eventService.publishEvent(
					new GroupDeletedEvent(this, getCurrentSession(), realm, provider, group, assosiatedPrincipals));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e, getCurrentSession(), realm, provider,
					group.getPrincipalName(), assosiatedPrincipals));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e, getCurrentSession(), realm, provider,
					group.getPrincipalName(), assosiatedPrincipals));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(new GroupDeletedEvent(this, e, getCurrentSession(), realm, provider,
					group.getPrincipalName(), assosiatedPrincipals));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "deleteGroup.unexpectedError", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteUser(Realm realm, Principal user, boolean deleteLocallyOnly) throws ResourceException, AccessDeniedException {

		final RealmProvider provider = getProviderForPrincipal(user);

		try {
			assertAnyPermission(UserPermission.DELETE, RealmPermission.DELETE);
			
			try {
				delegationService.assertDelegation(user);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}

			if (permissionService.hasSystemPermission(user)) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.cannotDeleteSystemAdmin",
						user.getPrincipalName());
			}
			
			permissionService.revokePermissions(user, new TransactionAdapter<Principal>() {
				@Override
				public void afterOperation(Principal resource, Map<String, String> properties)
						throws ResourceException {
					try {
						provider.deleteUser(resource, deleteLocallyOnly);
					} catch (ResourceChangeException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
			});

			eventService.publishEvent(new UserDeletedEvent(this, getCurrentSession(), realm, provider, user));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(
					new UserDeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(
					new UserDeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(
					new UserDeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "deleteUser.unexpectedError", e.getMessage());
		}

	}

	@Override
	public String getPrincipalAddress(Principal principal, MediaType type) throws MediaNotFoundException {

		UserPrincipal<?> user = (UserPrincipal<?>) principal;
		switch (type) {
		case EMAIL:
			if (StringUtils.isNotBlank(user.getEmail())) {
				return user.getEmail();
			} else if (StringUtils.isNotBlank(user.getSecondaryEmail())) {
				return ResourceUtils.explodeValues(user.getSecondaryEmail())[0];
			}
			throw new MediaNotFoundException();
		case PHONE:
			if (StringUtils.isNotBlank(user.getMobile())) {
				return user.getMobile();
			} else if (StringUtils.isNotBlank(user.getSecondaryMobile())) {
				return ResourceUtils.explodeValues(user.getSecondaryMobile())[0];
			}
			throw new MediaNotFoundException();
		default:
			throw new MediaNotFoundException();
		}

	}

	@Override
	public Collection<PropertyCategory> getGroupPropertyTemplates(Principal principal) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForPrincipal(principal);

		return provider.getGroupProperties(principal);
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(Principal principal) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForPrincipal(principal);

		return provider.getUserProperties(principal);
	}

	@Override
	public Collection<PropertyCategory> getUserProfileTemplates(Principal principal) throws AccessDeniedException {

		assertAnyPermission(ProfilePermission.READ);

		RealmProvider provider = getProviderForPrincipal(principal);
		RealmProvider realmProvider = getProviderForRealm(principal.getRealm());

		Collection<PropertyCategory> ret = provider.getUserProperties(principal);

		Set<String> editable = new HashSet<String>(
				Arrays.asList(configurationService.getValues(principal.getRealm(), "realm.userEditableProperties")));
		Set<String> visible = new HashSet<String>(
				Arrays.asList(configurationService.getValues(principal.getRealm(), "realm.userVisibleProperties")));

		/**
		 * Filter the properties down to read only and editable as defined by the realm
		 * configuration.
		 */

		List<PropertyCategory> results = new ArrayList<PropertyCategory>();

		for (PropertyCategory c : ret) {

			List<AbstractPropertyTemplate> tmp = new ArrayList<AbstractPropertyTemplate>();

			for (AbstractPropertyTemplate t : c.getTemplates()) {

				if (c.isUserCreated()) {
					/**
					 * Custom user created properties
					 */
					if (t.getDisplayMode() != null && t.getDisplayMode().equals("admin")) {
						tmp.add(t);
					}

				} else {
					/**
					 * These are built-in realm properties
					 */
					if (!realmProvider.equals(provider)) {
						if (t.getDisplayMode() != null && t.getDisplayMode().equals("admin")) {
							tmp.add(t);
						}
						continue;
					} else {
						if (!editable.contains(t.getResourceKey())) {
							if (!visible.contains(t.getResourceKey())) {
								tmp.add(t);
								continue;
							}
							t.getAttributes().put("disabled", "true");
							//t.setReadOnly(true);
							continue;
						}
					}
				}
			}

			c.getTemplates().removeAll(tmp);

			if (c.getTemplates().size() > 0) {
				results.add(c);
			}
		}

		return results;
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates(String module) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getUserProperties(null);
	}

	@Override
	public Collection<PropertyCategory> getUserPropertyTemplates() throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(getCurrentRealm());

		return provider.getUserProperties(null);
	}

	@Override
	public Collection<String> getUserPropertyNames(Realm realm, Principal principal) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = principal != null ? getProviderForPrincipal(principal) : getProviderForRealm(realm);

		return provider.getUserPropertyNames(principal);

	}

	@Override
	public Collection<String> getEditablePropertyNames(Realm realm) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);

		return provider.getEditablePropertyNames(realm);

	}

	@Override
	public Collection<String> getVisiblePropertyNames(Realm realm) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(realm);

		return provider.getVisiblePropertyNames(realm);

	}

	@Override
	public Collection<String> getUserPropertyNames(String module) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getUserPropertyNames(null);
	}

	@Override
	public Collection<PropertyCategory> getGroupPropertyTemplates(String module) throws AccessDeniedException {

		assertAnyPermission(GroupPermission.READ, RealmPermission.READ);

		RealmProvider provider = getProviderForRealm(module);

		return provider.getGroupProperties(null);
	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal) {

		Collection<Principal> result = getProviderForPrincipal(principal).getAssociatedPrincipals(principal);
		if (!result.contains(principal)) {
			result.add(principal);
		}
		return new ArrayList<Principal>(result);
	}

	@Override
	public List<Principal> getUserGroups(Principal principal) {
		return getProviderForPrincipal(principal).getUserGroups(principal);
	}

	@Override
	public List<Principal> getGroupUsers(Principal principal) {
		return getProviderForPrincipal(principal).getGroupUsers(principal);
	}

	@Override
	public List<Principal> getGroupGroups(Principal principal) {
		return getProviderForPrincipal(principal).getGroupGroups(principal);
	}

	@Override
	public List<Principal> getAssociatedPrincipals(Principal principal, PrincipalType type) {
		List<Principal> result = getProviderForPrincipal(principal).getAssociatedPrincipals(principal, type);
		if (!result.contains(principal) && principal.getType()==type) {
			result.add(principal);
		}
		return result;
	}

	@Override
	public List<?> searchPrincipals(Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			int start, int length, ColumnSort[] sorting) throws AccessDeniedException {
		return searchPrincipals(realm, type, null, searchColumn, searchPattern, start, length, sorting);
	}

	@Override
	public List<?> searchPrincipals(Realm realm, PrincipalType type, String module, String searchColumn,
			String searchPattern, int start, int length, ColumnSort[] sorting) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		switch (type) {
		case USER: {
			if (StringUtils.isNotBlank(module)) {
				TableFilter filter = principalFilters.get(module);
				if (filter == null) {
					filter = builtInPrincipalFilters.get(module);
				}
				return filter.searchResources(realm, searchColumn, searchPattern, start, length, sorting);
			} else {
				return principalRepository.search(realm, type, searchColumn, searchPattern, start, length, sorting);
			}
		}
		default: {
			if (StringUtils.isNotBlank(module)) {
				RealmProvider provider = getProviderForRealm(module);
				return provider.getPrincipals(realm, type, searchColumn, searchPattern, start, length, sorting);
			} else {
				return principalRepository.search(realm, type, searchColumn, searchPattern, start, length, sorting);
			}
		}
		}
	}

	@Override
	public Long getSearchPrincipalsCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern)
			throws AccessDeniedException {
		return getSearchPrincipalsCount(realm, type, null, searchColumn, searchPattern);
	}

	@Override
	public Long getSearchPrincipalsCount(Realm realm, PrincipalType type, String module, String searchColumn,
			String searchPattern) throws AccessDeniedException {

		assertAnyPermission(UserPermission.READ, GroupPermission.READ, ProfilePermission.READ, RealmPermission.READ);

		switch (type) {
		case USER: {
			if (StringUtils.isNotBlank(module)) {
				TableFilter filter = principalFilters.get(module);
				if (filter == null) {
					filter = builtInPrincipalFilters.get(module);
				}
				return filter.searchResourcesCount(realm, searchColumn, searchPattern);
			} else {
				return principalRepository.getResourceCount(realm, type, searchColumn, searchPattern);
			}
		}
		default: {
			if (StringUtils.isNotBlank(module)) {
				RealmProvider provider = getProviderForRealm(module);
				return provider.getPrincipalCount(realm, type, searchColumn, searchPattern);
			} else {
				return principalRepository.getResourceCount(realm, type, searchColumn, searchPattern);
			}
		}
		}
	}

	@Override
	public boolean findUniquePrincipal(String user) {

		int found = 0;
		for (Realm r : internalAllRealms()) {
			Principal p = getPrincipalByName(r, user, PrincipalType.USER);
			if (p != null) {
				found++;
			}
		}
		return found == 1;
	}

	@Override
	public Principal getUniquePrincipal(String username, PrincipalType... type) throws ResourceNotFoundException {

		String realmName = null;
		// Can we extract realm from username?
		int idx;
		idx = username.indexOf('\\');
		if (idx > -1) {
			realmName = username.substring(0, idx);
			username = username.substring(idx + 1);
		} else {
			idx = username.indexOf('/');
			if (idx > -1) {
				realmName = username.substring(0, idx);
				username = username.substring(idx + 1);
			}
		}

		if (realmName != null) {
			Realm realm = getRealmByName(realmName);
			if (realm == null) {
				throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.invalidRealm", realmName);
			}
			return getPrincipalByName(realm, username, type);
		}

		Collection<Principal> found = principalRepository.getPrincpalsByName(username, type);
		return selectPrincipal(found, username);
	}

	@Override
	public Principal getUniquePrincipalForRealm(String username, Realm realm, PrincipalType... type)
			throws ResourceNotFoundException {
		Collection<Principal> found = principalRepository.getPrincpalsByName(username, realm, type);
		if (!found.isEmpty()) {
			return selectPrincipal(found, username);
		}
		return getProviderForRealm(realm).getPrincipalByName(username, realm, type);
	}

	protected Principal selectPrincipal(Collection<Principal> found, String username) throws ResourceNotFoundException {
		if (found.size() != 1) {
			if (found.size() > 1) {
				// Fire Event
				if (log.isInfoEnabled()) {
					log.info("More than one principal found for username " + username);
				}
				for (Principal principal : found) {
					if (principal.getRealm().isDeleted()) {
						continue;
					}
					if (principal.isSystem() || permissionService.hasAdministrativePermission(principal)) {
						log.info(String.format("Resolving duplicate principals to %s/%s [System User]",
								principal.getRealm().getName(), principal.getPrincipalName()));
						return principal;
					}
				}
				for (Principal principal : found) {
					if (principal.getRealm().isDeleted()) {
						continue;
					}
					if (principal.getRealm().isDefaultRealm()) {
						log.info(String.format("Resolving duplicate principals to %s/%s [Default Realm]",
								principal.getRealm().getName(), principal.getPrincipalName()));
						return principal;
					}
				}

			}
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "principal.notFound");
		}
		return found.iterator().next();
	}

	@Override
	public List<Realm> getRealms(String searchPattern, String searchColumn, int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException {

		assertAnyPermission(RealmPermission.READ, SystemPermission.SWITCH_REALM);

		return realmRepository.searchRealms(searchPattern, searchColumn, start, length, sorting, getCurrentRealm(),
				permissionService.hasSystemPermission(getCurrentPrincipal()) ? Collections.<Realm>emptyList()
						: permissionService.getPrincipalPermissionRealms(getCurrentPrincipal()));
	}

	@Override
	public Long getRealmCount(String searchPattern, String searchColumn) {

		return realmRepository.countRealms(searchPattern, searchColumn, getCurrentRealm(),
				permissionService.hasSystemPermission(getCurrentPrincipal()) ? Collections.<Realm>emptyList()
						: permissionService.getPrincipalPermissionRealms(getCurrentPrincipal()));
	}
	

	@Override
	public Long getRealmCount() {
		return getRealmCount("name", "");
	}

	@Override
	public void updateProfile(Realm realm, Principal principal, Map<String, String> properties)
			throws AccessDeniedException, ResourceException {

		RealmProvider provider = getProviderForPrincipal(principal);
		RealmProvider realmProvider = getProviderForRealm(principal.getRealm());
		Map<String, String> changedProperties = new HashMap<String, String>();

		if (realmProvider.equals(provider)) {
			/**
			 * This ensures we only ever update those properties that are allowed
			 */
			String[] editableProperties = configurationService.getValues(realm, "realm.userEditableProperties");

			Collection<PropertyTemplate> userAttributes = userAttributeService.getPropertyResolver()
					.getPropertyTemplates(principal);

			for (String allowed : editableProperties) {
				if (properties.containsKey(allowed)) {
					changedProperties.put(allowed, properties.get(allowed));
				}
			}

			for (PropertyTemplate t : userAttributes) {
				if (properties.containsKey(t.getResourceKey())) {
					if (t.getDisplayMode() == null || !t.getDisplayMode().equals("admin")) {
						changedProperties.put(t.getResourceKey(), properties.get(t.getResourceKey()));
					}
				}
			}
		} else {
			changedProperties.putAll(properties);
		}

		try {
			assertAnyPermission(ProfilePermission.UPDATE, RealmPermission.UPDATE, UserPermission.UPDATE);

			try {
				delegationService.assertDelegation(principal);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			principal = provider.updateUserProperties(principal, changedProperties);

			eventService.publishEvent(new ProfileUpdatedEvent(this, getCurrentSession(), realm, provider, principal,
					filterSecretProperties(principal, provider, changedProperties)));
		} catch (AccessDeniedException e) {
			eventService.publishEvent(new ProfileUpdatedEvent(this, e, getCurrentSession(), realm, provider,
					principal.getPrincipalName(), filterSecretProperties(principal, provider, changedProperties)));
			throw e;
		} catch (ResourceException e) {
			eventService.publishEvent(new ProfileUpdatedEvent(this, e, getCurrentSession(), realm, provider,
					principal.getPrincipalName(), filterSecretProperties(principal, provider, changedProperties)));
			throw e;
		}

	}

	@Override
	public String getPrincipalDescription(Principal principal) {

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		return provider.getPrincipalDescription(principal);
	}

	@Override
	public boolean supportsAccountUnlock(Realm realm) throws ResourceException {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.supportsAccountUnlock(realm);
	}

	@Override
	public boolean supportsAccountDisable(Realm realm) throws ResourceException {

		RealmProvider provider = getProviderForRealm(realm);

		return provider.supportsAccountDisable(realm);
	}

	@Override
	public Principal disableAccount(Principal principal) throws AccessDeniedException, ResourceException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		try {
			if (provider.isReadOnly(principal.getRealm())) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}
			
			try {
				delegationService.assertDelegation(principal);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			principal = provider.disableAccount(principal);
			eventService.publishEvent(
					new AccountDisabledEvent(this, getCurrentSession(), provider, getCurrentPrincipal(), principal));
		} catch (ResourceException re) {
			eventService.publishEvent(new AccountDisabledEvent(this, re, getCurrentSession(), provider,
					getCurrentPrincipal(), principal));
			throw re;
		}

		return principal;

	}

	@Override
	public Principal enableAccount(Principal principal) throws AccessDeniedException, ResourceException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

		RealmProvider provider = getProviderForRealm(principal.getRealm());

		if (provider.isReadOnly(principal.getRealm())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
		}

		try {
			
			try {
				delegationService.assertDelegation(principal);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			if (provider.isReadOnly(principal.getRealm())) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}
			principal = provider.enableAccount(principal);
			eventService.publishEvent(
					new AccountEnabledEvent(this, getCurrentSession(), provider, getCurrentPrincipal(), principal));
		} catch (ResourceException re) {
			eventService.publishEvent(
					new AccountEnabledEvent(this, re, getCurrentSession(), provider, getCurrentPrincipal(), principal));
			throw re;
		}

		return principal;
	}

	@Override
	public Principal unlockAccount(Principal principal) throws AccessDeniedException, ResourceException {

		assertAnyPermission(UserPermission.UPDATE, RealmPermission.UPDATE);

		try {
			delegationService.assertDelegation(principal);
		} catch (AccessDeniedException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
		}
		
		RealmProvider provider = getProviderForRealm(principal.getRealm());

		if (provider.isReadOnly(principal.getRealm())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
		}

		return provider.unlockAccount(principal);
	}

	@Override
	public void registerRealmListener(RealmListener listener) {
		realmListeners.add(listener);
	}

	@Override
	public Realm getDefaultRealm() {
		return realmRepository.getDefaultRealm();
	}

	class RealmPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getPropertyNames(null);
		}

	}

	class UserPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getUserPropertyNames(null);
		}
	}

	class GroupPropertyCollector implements EventPropertyCollector {

		@Override
		public Set<String> getPropertyNames(String resourceKey, Realm realm) {
			RealmProvider provider = getProviderForRealm(realm);
			return provider.getGroupPropertyNames(null);
		}

	}

	@Override
	public boolean isRealmStrictedToHost(Realm realm) {

		if (realm == null) {
			return false;
		}
		RealmProvider realmProvider = getProviderForRealm(realm);
		return realmProvider.getBooleanValue(realm, "realm.hostRestriction");

	}

	@Override
	public Collection<String> getUserVariableNames(Realm realm, Principal principal) {

		RealmProvider provider = getProviderForRealm(realm);

		Set<String> tmp = new HashSet<String>(UserVariableReplacementServiceImpl.getDefaultReplacements());
		tmp.addAll(provider.getUserVariableNames(principal));
		return tmp;

	}

	@Override
	public Collection<String> getAllUserAttributeNames(Realm realm) {
		RealmProvider provider = getProviderForRealm(realm);
		Set<String> names = new LinkedHashSet<>();
		for (PropertyCategory cat : provider.getPrincipalTemplate(realm)) {
			for (AbstractPropertyTemplate temp : cat.getTemplates()) {
				if (StringUtils.isNotBlank(temp.getName())) {
					names.add(temp.getName());
				} else if (StringUtils.isNotBlank(temp.getResourceKey())) {
					names.add(temp.getResourceKey());
				} 
			}
		}
		for (PropertyCategory cat : provider.getUserProperties(null)) {
			if(!cat.isHidden()) {
				for (AbstractPropertyTemplate temp : cat.getTemplates()) {
					if(!temp.isHidden()) {
						if (StringUtils.isNotBlank(temp.getName())) {
							names.add(temp.getName());
						} else if (StringUtils.isNotBlank(temp.getResourceKey())) {
							names.add(temp.getResourceKey());
						} 
					}
				}
			}
		}
		names.addAll(provider.getDefaultUserPropertyNames());
		names.addAll(DEFAULT_PRINCIPAL_ATTRIBUTE_NAMES);
		return names;
	}

	@Override
	public String getPrincipalEmail(Principal principal) {
		return principal.getEmail();
	}

	@Override
	public String getPrincipalPhone(Principal principal) {
		try {
			return getPrincipalAddress(principal, MediaType.PHONE);
		} catch (MediaNotFoundException e) {
			return "";
		}
	}

	@Override
	public String getProfileProperty(Principal principal, String resourceKey) {

		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserPropertyValue(principal, resourceKey);
	}

	@Override
	public Map<String, String> getUserPropertyValues(Principal principal, String... variableNames) {

		Map<String, String> variables = new HashMap<String, String>();

		for (String variableName : variableNames) {
			variables.put(variableName, userVariableReplacement.getVariableValue(principal, variableName));
		}

		return variables;
	}

	@Override
	public Map<String, String> getUserPropertyValues(Principal principal) {

		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserPropertyValues(principal);
	}

	@Override
	public Long getUserPropertyLong(Principal principal, String resourceKey) {
		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserPropertyLong(principal, resourceKey);

	}

	@Override
	public Integer getUserPropertyInt(Principal principal, String resourceKey) {
		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserPropertyInt(principal, resourceKey);
	}

	@Override
	public boolean getUserPropertyBoolean(Principal principal, String resourceKey) {
		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserPropertyBoolean(principal, resourceKey);
	}

	@Override
	public String getUserProperty(Principal principal, String resourceKey) {
		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserProperty(principal, resourceKey);
	}

	@Override
	public long getPrincipalCount(Realm realm, PrincipalType type) {
		return principalRepository.getResourceCount(realm, type);
	}

	@Override
	public long getPrincipalCount(Collection<Realm> realms, PrincipalType type) {
		return principalRepository.getResourceCount(realms, type);
	}

	@Override
	public boolean canChangePassword(Principal principal) {

		RealmProvider provider = getProviderForPrincipal(principal);

		return provider.canChangePassword(principal);
	}

	@Override
	public Collection<PropertyCategory> getUserProperties(Principal principal) {

		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.getUserProperties(principal);
	}

	@Override
	public void setUserPropertyLong(Principal principal, String resourceKey, Long val) {
		RealmProvider provider = getProviderForPrincipal(principal);
		provider.setUserProperty(principal, resourceKey, val);
	}

	@Override
	public void setUserPropertyInt(Principal principal, String resourceKey, Integer val) {
		RealmProvider provider = getProviderForPrincipal(principal);
		provider.setUserProperty(principal, resourceKey, val);
	}

	@Override
	public void setUserPropertyBoolean(Principal principal, String resourceKey, Boolean val) {
		RealmProvider provider = getProviderForPrincipal(principal);
		provider.setUserProperty(principal, resourceKey, val);
	}

	@Override
	public void setUserProperty(Principal principal, String resourceKey, String val) {
		RealmProvider provider = getProviderForPrincipal(principal);
		provider.setUserProperty(principal, resourceKey, val);
	}

	@Override
	public void deleteRealms(final List<Realm> resources) throws ResourceException, AccessDeniedException {
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				for (Realm realm : resources) {
					try {
						deleteRealm(realm);
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
				return null;
			}
		});

	}

	@Override
	public void assignUserToGroup(Principal user, Principal group) throws ResourceException, AccessDeniedException {

		assertPermission(GroupPermission.UPDATE);
		
		try {
			delegationService.assertDelegation(user);
			delegationService.assertDelegation(group);
		} catch (AccessDeniedException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
		}
		
		List<Principal> groups = new ArrayList<Principal>(getUserGroups(user));
		groups.add(group);

		updateUser(user.getRealm(), user, user.getPrincipalName(), new HashMap<String, String>(), groups);
	}

	@Override
	public void unassignUserFromGroup(Principal user, Principal group) throws ResourceException, AccessDeniedException {

		assertPermission(GroupPermission.UPDATE);
		
		try {
			delegationService.assertDelegation(user);
			delegationService.assertDelegation(group);
		} catch (AccessDeniedException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
		}
		
		List<Principal> groups = new ArrayList<Principal>(getUserGroups(user));
		groups.remove(group);

		updateUser(user.getRealm(), user, user.getPrincipalName(), new HashMap<String, String>(), groups);
	}

	@Override
	public List<Realm> getRealmsByIds(Long... ids) throws AccessDeniedException {
		assertPermission(RealmPermission.READ);
		return realmRepository.getRealmsByIds(ids);
	}

	@Override
	public void deleteUsers(final Realm realm, final List<Principal> users, boolean deleteLocallyOnly)
			throws ResourceException, AccessDeniedException {
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				for (Principal user : users) {
					try {
						deleteUser(realm, user, deleteLocallyOnly);
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
				return null;
			}
		});
	}

	@Override
	public List<Principal> getUsersByIds(Long... ids) throws AccessDeniedException {
		return getPrincipalsByIds(ids);
	}

	@Override
	public void deleteGroups(final Realm realm, final List<Principal> groups, boolean deleteLocallyOnly)
			throws ResourceException, AccessDeniedException {
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				for (Principal group : groups) {
					try {
						deleteGroup(realm, group, deleteLocallyOnly);
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
				return null;
			}
		});

	}

	@Override
	public List<Principal> getGroupsByIds(Long... ids) throws AccessDeniedException {
		return getPrincipalsByIds(ids);
	}

	private List<Principal> getPrincipalsByIds(Long... ids) throws AccessDeniedException {
		List<Principal> principals = new ArrayList<>();
		for (Long id : ids) {
			Principal principal = getPrincipalById(id);
			if (principal == null) {
				throw new IllegalStateException(String.format("Principal by id %d not found.", id));
			}
			principals.add(principal);
		}
		return principals;
	}

	@Override
	public boolean isLocked(Principal principal) throws ResourceException {

		RealmProvider provider = getProviderForPrincipal(principal);
		principal = provider.reconcileUser(principal);

		return principal.getPrincipalStatus() == PrincipalStatus.LOCKED;
	}

	@Override
	public boolean isUserSelectingRealm() {
		return systemConfigurationService.getBooleanValue("auth.chooseRealm");
	}

	@Override
	public String[] getRealmHostnames(Realm realm) {
		RealmProvider provder = getProviderForRealm(realm);
		return provder.getValues(realm, "realm.host");
	}

	@Override
	public Collection<TableFilter> getPrincipalFilters() {
		Collection<TableFilter> filters = new ArrayList<>();
		for(TableFilter filter : principalFilters.values()) {
			if(filter.isEnabled(getCurrentRealm())) {
				filters.add(filter);
			}
		}
		
		return filters;
	}

	

	@Override
	public boolean isDisabled(Principal principal) {
		RealmProvider provider = getProviderForPrincipal(principal);
		return provider.isDisabled(principal);
	}

	

	@Override
	public Collection<Realm> getRealmsByOwner() {

		if (getCurrentRealm().isSystem()) {
			try {
				assertAnyPermission(SystemPermission.SYSTEM_ADMINISTRATION, SystemPermission.SYSTEM);
				return allRealms();
			} catch (AccessDeniedException e) {
			}
		}

		Set<Realm> realms = new HashSet<Realm>();
		realms.add(getCurrentRealm());
		realms.addAll(realmRepository.getRealmsByParent(getCurrentRealm()));

		for (RealmOwnershipResolver resolver : ownershipResolvers) {
			realms.addAll(resolver.resolveRealms(getCurrentPrincipal()));
		}
		return realms;
	}

	@Override
	public Collection<Realm> getRealmsByParent(Realm currentRealm) {
		return realmRepository.getRealmsByParent(currentRealm);
	}
	
	@Override
	public Collection<Realm> getPublicRealmsByParent(Realm currentRealm) {
		return realmRepository.getPublicRealmsByParent(currentRealm);
	}

	@Override
	public Map<String, String> getRealmProperties(Realm realm) {
		RealmProvider provider = getProviderForRealm(realm);
		return provider.getProperties(realm);
	}

	@Override
	public List<CommonEndOfLine> getCommonEndOfLine() {
		List<CommonEndOfLine> eolList = new ArrayList<CommonEndOfLine>();
		for (CommonEndOfLineEnum eol : CommonEndOfLineEnum.values()) {
			CommonEndOfLine aux = new CommonEndOfLine(eol.getValue(), eol.name());
			eolList.add(aux);
		}
		return eolList;
	}

	@Override
	public void downloadCSV(final Realm realm, final String searchColumn, final String searchPattern,
			final String module, String filename, boolean outputHeaders, String delimiters, CommonEndOfLineEnum eol,
			String wrap, String escape, final String attributes, final ColumnSort[] sort, OutputStream output,
			final Locale locale) throws AccessDeniedException, UnsupportedEncodingException {
		// TODO hmm?
		// assertPermission(RealmPermission.READ);

		exportService.downloadCSV(realm, new AbstractPagingExportDataProvider<UserPrincipal<?>>(PAGE_SIZE) {
			Set<String> includeAttributes;
			Cache<String, String> i18n;
			Realm currentRealm = realm;
			List<String> attrs;

			{
				if (currentRealm == null) {
					currentRealm = getCurrentRealm();
				}

				includeAttributes = new LinkedHashSet<String>();
				if(StringUtils.isNotBlank(attributes))
					attrs = Arrays.asList(attributes.split(","));
				if(attrs == null || attrs.isEmpty())
					includeAttributes.addAll(DEFAULT_PRINCIPAL_ATTRIBUTE_NAMES);
				if(attrs != null) {
					includeAttributes.addAll(attrs);
				}
				i18n = i18nService.getResourceMap(locale);
			}

			@Override
			public Collection<String> getHeaders() {
				return includeAttributes;
			}

			protected Map<String, String> convertToMap(UserPrincipal<?> princ) {
				final Map<String, String> map = new HashMap<String, String>();
				if(includeAttributes.contains(TEXT_REALM)) 
					map.put(TEXT_REALM, princ.getRealm().getName());
				if(includeAttributes.contains(TEXT_PRINCIPAL_NAME)) 
					map.put(TEXT_PRINCIPAL_NAME, princ.getPrincipalName());
				if(includeAttributes.contains(TEXT_NAME)) 
					map.put(TEXT_NAME, i18n.get(princ.getName()));
				if(includeAttributes.contains(TEXT_EMAIL)) 
					map.put(TEXT_EMAIL, princ.getEmail());
				if(includeAttributes.contains(TEXT_OU)) 
					map.put(TEXT_OU, princ.getOrganizationalUnit());
				if(includeAttributes.contains(TEXT_PRIMARY_EMAIL)) 
					map.put(TEXT_PRIMARY_EMAIL, princ.getPrimaryEmail());
				if(includeAttributes.contains(TEXT_DESCRIPTION)) 
					map.put(TEXT_DESCRIPTION, princ.getDescription());
				if(includeAttributes.contains(TEXT_UUID)) 
					map.put(TEXT_UUID, princ.getUUID());
				if(includeAttributes.contains(TEXT_CREATE_DATE)) 
					map.put(TEXT_CREATE_DATE, princ.getCreateDate() == null ? ""
							: HypersocketUtils.formatDate(princ.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
				if(includeAttributes.contains(TEXT_MODIFIED_DATE)) 
					map.put(TEXT_MODIFIED_DATE, princ.getModifiedDate() == null ? ""
							: HypersocketUtils.formatDate(princ.getModifiedDate(), "yyyy-MM-dd HH:mm:ss"));
				if(includeAttributes.contains(TEXT_EXPIRES)) 
					map.put(TEXT_EXPIRES, princ.getExpires() == null ? ""
							: HypersocketUtils.formatDate(princ.getExpires(), "yyyy-MM-dd HH:mm:ss"));
				if(includeAttributes.contains(TEXT_STATUS)) 
					map.put(TEXT_STATUS, princ.getPrincipalStatus() == null ? "" : princ.getPrincipalStatus().name());
				if(includeAttributes.contains(TEXT_LAST_PASSWORD_CHANGE)) 
					map.put(TEXT_LAST_PASSWORD_CHANGE, princ.getLastPasswordChange() == null ? ""
							: HypersocketUtils.formatDate(princ.getLastPasswordChange(), "yyyy-MM-dd HH:mm:ss"));
				if(includeAttributes.contains(TEXT_PASSWORD_EXPIRY)) 
					map.put(TEXT_PASSWORD_EXPIRY, princ.getPasswordExpiry() == null ? ""
							: HypersocketUtils.formatDate(princ.getPasswordExpiry(), "yyyy-MM-dd HH:mm:ss"));
				if(includeAttributes.contains(TEXT_LAST_SIGN_ON)) 
					map.put(TEXT_LAST_SIGN_ON, princ.getLastSignOn() == null ? ""
							: HypersocketUtils.formatDate(princ.getLastSignOn(), "yyyy-MM-dd HH:mm:ss"));
				
				if(!attributes.isEmpty()){
					RealmProvider provider = getProviderForRealm(princ.getRealm());
					for(String a : attrs) {
						if(!map.containsKey(a))
							map.put(a, provider.getUserProperty(princ, a));
					}
				}
				
				final Map<String, String> properties = princ.getProperties();
				if (properties != null)
					map.putAll(properties);
				return map;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected List<UserPrincipal<?>> fetchPage(int startPosition) throws AccessDeniedException {
				return (List<UserPrincipal<?>>) searchPrincipals(currentRealm, PrincipalType.USER, module, searchColumn,
						searchPattern, startPosition, PAGE_SIZE, sort);
			}
		}, outputHeaders, delimiters, eol, wrap, escape, "", output, locale);

	}

	@Override
	public void undeleteUser(Realm realm, Principal user) throws ResourceException, AccessDeniedException {
		final RealmProvider provider = getProviderForPrincipal(user);

		try {
			assertAnyPermission(UserPermission.CREATE, RealmPermission.CREATE);

			if (provider.isReadOnly(realm)) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.realmIsReadOnly");
			}

			try {
				delegationService.assertDelegation(user);
			} catch (AccessDeniedException e) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
			}
			
			principalRepository.undelete(realm, user);

			eventService.publishEvent(new UserUndeletedEvent(this, getCurrentSession(), realm, provider, user));

		} catch (AccessDeniedException e) {
			eventService.publishEvent(
					new UserUndeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw e;
		} catch (ResourceChangeException e) {
			eventService.publishEvent(
					new UserUndeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw e;
		} catch (ResourceException e) {
			eventService.publishEvent(
					new UserUndeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw e;
		} catch (Throwable e) {
			eventService.publishEvent(
					new UserUndeletedEvent(this, e, getCurrentSession(), realm, provider, user.getPrincipalName()));
			throw new ResourceChangeException(e, RESOURCE_BUNDLE, "undeleteUser.unexpectedError", e.getMessage());
		}
		
	}

	@Override
	public void undeleteUsers(Realm realm, List<Principal> users) throws ResourceException, AccessDeniedException {
		transactionService.doInTransaction(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				for (Principal user : users) {
					try {
						undeleteUser(realm, user);
					} catch (ResourceException | AccessDeniedException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
				return null;
			}
		});
		
	}

	@Override
	public Principal getPrincipalByUUID(String uuid) {
		Principal principal = principalRepository.getResourceByUUID(uuid);
		return principal;
	}
	
	private String computePropertyChanges(Realm realm, Map<String, String> properties) throws AccessDeniedException {
		List<PropertyChange> changes = calculateChanges(realm, properties);
		
		String propertyChangesCSV = "";
		if (!changes.isEmpty()) {
			String template = i18nService.getResource("realm.update.propertyChangeRecordTemplate", getCurrentLocale());
			List<String> records = new ArrayList<>();
			
			for (PropertyChange propertyChange : changes) {
				String record = String.format(template, 
						propertyChange.getId(), 
						propertyChange.getOldValue(), 
						propertyChange.getNewValue());
				
				records.add(record);
				
				
				
			}
			
			propertyChangesCSV = StringUtils.join(records, ", ");
		}
		return propertyChangesCSV;
	}
	
	private List<PropertyChange> calculateChanges(Realm resource, Map<String,String> properties) throws AccessDeniedException {
		List<PropertyChange> changedProperties = new ArrayList<>();
		if(properties != null) {
			for(PropertyCategory category: getRealmPropertyTemplates(resource)) {
				List<AbstractPropertyTemplate> propertyTemplates = category.getTemplates();
				
				List<PropertyChange> changes = realmRepository.calculateChanges(resource, propertyTemplates, properties);
				
				changedProperties.addAll(changes);
			}
			
			
		}
		return changedProperties;
	}

	@Override
	public void assertChangeCredentials(Principal principal) throws AccessDeniedException, ResourceException {
		
		if(getCurrentPrincipal().equals(principal)) {
			return;
		}
		
		assertAnyPermission(UserPermission.UPDATE, UserPermission.RESET_CREDENTIALS);
		
		try {
			delegationService.assertDelegation(principal);
		} catch (AccessDeniedException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.noDelegation");
		}
	}
}
