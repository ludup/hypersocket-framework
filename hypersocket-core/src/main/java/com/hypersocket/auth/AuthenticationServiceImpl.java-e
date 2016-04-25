/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserVariableReplacement;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

@Service
@Transactional
public class AuthenticationServiceImpl extends
		PasswordEnabledAuthenticatedServiceImpl implements
		AuthenticationService {

	public static final String BROWSER_AUTHENTICATION_SCHEME = "Browser";
	public static final String BROWSER_AUTHENTICATION_RESOURCE_KEY = "basic";

	public static final String ANONYMOUS_AUTHENTICATION_SCHEME = "Anonymous";
	public static final String ANONYMOUS_AUTHENTICATION_RESOURCE_KEY = "anonymous";

	private static Logger log = LoggerFactory
			.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	PermissionService permissionService;

	@Autowired
	AuthenticationModuleRepository repository;

	@Autowired
	AuthenticationSchemeRepository schemeRepository;

	@Autowired
	SessionService sessionService;

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	RealmService realmService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService;

	@Autowired
	UserVariableReplacement variableReplacement;
	
	Map<String, Authenticator> authenticators = new HashMap<String, Authenticator>();

	List<PostAuthenticationStep> postAuthenticationSteps = new ArrayList<PostAuthenticationStep>();

	List<AuthenticationServiceListener> listeners = new ArrayList<AuthenticationServiceListener>();

	Permission logonPermission;

	@PostConstruct
	private void postConstruct() {

		if (log.isInfoEnabled()) {
			log.info("Configuring Authentication Service");
		}

		PermissionCategory authentication = permissionService
				.registerPermissionCategory(RESOURCE_BUNDLE,
						"category.authentication");

		logonPermission = permissionService.registerPermission(
				AuthenticationPermission.LOGON, authentication);

		eventService.registerEvent(AuthenticationAttemptEvent.class,
				RESOURCE_BUNDLE);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		setupRealms();
	}

	private void setupRealms() {

		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return schemeRepository.getSchemeByResourceKeyCount(realm,
						BROWSER_AUTHENTICATION_RESOURCE_KEY) > 0;
			}

			@Override
			public void onCreateRealm(Realm realm) {

				if (log.isInfoEnabled()) {
					log.info("Creating " + ANONYMOUS_AUTHENTICATION_SCHEME
							+ " authentication scheme for realm "
							+ realm.getName());
				}

				List<String> modules = new ArrayList<String>();
				schemeRepository.createScheme(realm,
						ANONYMOUS_AUTHENTICATION_SCHEME, modules,
						ANONYMOUS_AUTHENTICATION_RESOURCE_KEY, true, 0,
						AuthenticationModuleType.HIDDEN);

				if (log.isInfoEnabled()) {
					log.info("Creating " + BROWSER_AUTHENTICATION_SCHEME
							+ " authentication scheme for realm "
							+ realm.getName());
				}

				modules.add(UsernameAndPasswordAuthenticator.RESOURCE_KEY);
				schemeRepository.createScheme(realm,
						BROWSER_AUTHENTICATION_SCHEME, modules,
						BROWSER_AUTHENTICATION_RESOURCE_KEY, false, 10,
						AuthenticationModuleType.HTML);

			}
		});

	}

	@Override
	public void registerListener(AuthenticationServiceListener listener) {
		listeners.add(listener);
	}

	@Override
	public void registerAuthenticator(Authenticator authenticator) {

		if (authenticators.containsKey(authenticator.getResourceKey()))
			throw new IllegalArgumentException("Duplicate Authenticator "
					+ authenticator.getResourceKey());

		if (log.isInfoEnabled()) {
			log.info("Registering " + authenticator.getResourceKey()
					+ " authenticator");
		}

		authenticators.put(authenticator.getResourceKey(), authenticator);
	}

	@Override
	public Authenticator getAuthenticator(String resourceKey) {
		return authenticators.get(resourceKey);
	}

	@Override
	public AuthenticationScheme getDefaultScheme(String remoteAddress,
			Map<String, String> environment, Realm realm) {
		List<AuthenticationScheme> schemes = schemeRepository.allSchemes(realm);
		if (schemes.size() == 0)
			throw new IllegalArgumentException(
					"There are no authentication schemes configured!");
		return schemes.get(0);
	}

	@Override
	public boolean isAuthenticatorInScheme(Realm realm,
			String schemeResourceKey, String resourceKey) {

		AuthenticationScheme s = schemeRepository.getSchemeByResourceKey(realm,
				schemeResourceKey);
		for (AuthenticationModule m : repository
				.getAuthenticationModulesByScheme(s)) {
			if (m.getTemplate().equals(resourceKey)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getAuthenticatorCount(Realm realm, String schemeResourceKey) {

		AuthenticationScheme s = schemeRepository.getSchemeByResourceKey(realm,
				schemeResourceKey);
		return repository.getAuthenticationModulesByScheme(s).size();
	}

	@Override
	public AuthenticationScheme getSchemeByResourceKey(Realm realm,
			String resourceKey) throws AccessDeniedException {
		return schemeRepository.getSchemeByResourceKey(realm, resourceKey);
	}

	@Override
	public AuthenticationState createAuthenticationState(
			String schemeResourceKey, String remoteAddress,
			Map<String, Object> environment, Locale locale)
			throws AccessDeniedException {

		AuthenticationState state = new AuthenticationState(remoteAddress,
				locale, environment);

		// Can we determine the principal from the current information
		if (environment
				.containsKey(BrowserEnvironment.AUTHORIZATION.toString())) {

			String header = environment.get(
					BrowserEnvironment.AUTHORIZATION.toString()).toString();
			if (header.toLowerCase().startsWith("basic")) {
				header = new String(Base64.decode(header.substring(6)));
				int idx = header.indexOf(':');
				if (idx > -1) {
					String username = header.substring(0, idx);
					String password = header.substring(idx + 1);
					state.addParameter(
							UsernameAndPasswordTemplate.USERNAME_FIELD,
							username);
					state.addParameter(
							UsernameAndPasswordTemplate.PASSWORD_FIELD,
							password);

				}
			}
		}

		if (state.getRealm() == null) {
			if (environment.containsKey(BrowserEnvironment.HOST.toString())) {

				// If not can we determine the realm from the current
				// information
				String hostHeader = environment.get(
						BrowserEnvironment.HOST.toString()).toString();
				int idx;
				if ((idx = hostHeader.indexOf(':')) > -1) {
					hostHeader = hostHeader.substring(0, idx);
				}

				state.setRealm(realmService.getRealmByHost(hostHeader));
			}
		}

		if (state.getRealm() == null) {
			state.setRealm(realmService.getDefaultRealm());
		}

		AuthenticationScheme scheme = getSchemeByResourceKey(state.getRealm(),
				schemeResourceKey);
		if (scheme == null) {
			if (log.isWarnEnabled()) {
				log.warn(schemeResourceKey
						+ " is not a valid authentication scheme");
			}
			scheme = getSchemeByResourceKey(
					state.getRealm(),
					AuthenticationServiceImpl.BROWSER_AUTHENTICATION_RESOURCE_KEY);
		}
		
		state.setScheme(scheme);
		state.setModules(repository.getModulesForScheme(state.getScheme()));

		return state;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException, FallbackAuthenticationRequired {

		state.setLastErrorMsg(null);
		state.setLastErrorIsResourceKey(false);

		boolean success = false;

		if (state.isAuthenticationComplete()) {

			if (state.getSession() != null) {
				setCurrentSession(state.getSession(), state.getLocale());
			}

			try {
				if (state.getCurrentPostAuthenticationStep() != null) {

					switch (state.getCurrentPostAuthenticationStep().process(
							state, parameterMap)) {
					case INSUFFICIENT_DATA: {
						if (state.getLastErrorMsg() == null) {
							state.setLastErrorMsg("error.insufficentData");
							state.setLastErrorIsResourceKey(true);
						}
						break;
					}
					case INSUFFICIENT_DATA_NO_ERROR: {
							state.setLastErrorMsg(null);
							state.setLastErrorIsResourceKey(false);
						break;
					}
					case AUTHENTICATION_SUCCESS: {

						state.nextPostAuthenticationStep();

						if (state.canCreateSession()) {
							state.setSession(completeLogon(state));
						}

						if (state.hasPostAuthenticationStep()) {
							PostAuthenticationStep step = state
									.getCurrentPostAuthenticationStep();
							if (!step.requiresUserInput(state)) {
								success = logon(state, parameterMap);
							}
						}
						success = true;
						break;
					}
					default: {
						state.setLastErrorMsg("error.genericLogonError");
						state.setLastErrorIsResourceKey(true);
						break;
					}
					}
				}
			} finally {
				clearPrincipalContext();
			}
		} else {
			Authenticator authenticator = authenticators.get(state
					.getCurrentModule().getTemplate());

			if(authenticator==null) {
				throw new FallbackAuthenticationRequired();
			}
			
			if (authenticator.isSecretModule()
					&& state.getPrincipal() instanceof FakePrincipal) {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationAttemptEvent(this,
						state, authenticator, "hint.invalidPrincipal"));
			} else {
				switch (authenticator.authenticate(state, parameterMap)) {
				case INSUFFICIENT_DATA: {
					if (state.getAttempts() >= 1) {
						state.setLastErrorMsg("error.insufficentData");
						state.setLastErrorIsResourceKey(true);
					}
					break;
				}
				case INSUFFICIENT_DATA_NO_ERROR: {
					state.setLastErrorMsg(null);
					state.setLastErrorIsResourceKey(false);
					break;
				}
				case AUTHENTICATION_FAILURE_INVALID_CREDENTIALS: {

					if (!authenticator.isSecretModule() && state.hasNextStep()) {
						state.fakeCredentials();
						state.nextModule();
					} else {
						state.setLastErrorMsg("error.genericLogonError");
						state.setLastErrorIsResourceKey(true);
						eventService
								.publishEvent(new AuthenticationAttemptEvent(
										this, state, authenticator,
										"hint.badCredentials"));
					}

					break;
				}
				case AUTHENTICATION_FAILURE_INVALID_PRINCIPAL: {

					if (!authenticator.isSecretModule() && state.hasNextStep()) {
						state.fakeCredentials();
						state.nextModule();
					} else {
						state.setLastErrorMsg("error.genericLogonError");
						state.setLastErrorIsResourceKey(true);
						eventService
								.publishEvent(new AuthenticationAttemptEvent(
										this, state, authenticator,
										"hint.invalidPrincipal"));
					}
					break;
				}
				case AUTHENTICATION_FAILURE_INVALID_REALM: {

					if (!authenticator.isSecretModule() && state.hasNextStep()) {
						state.fakeCredentials();
						state.nextModule();
					} else {
						state.setLastErrorMsg("error.genericLogonError");
						state.setLastErrorIsResourceKey(true);
						eventService
								.publishEvent(new AuthenticationAttemptEvent(
										this, state, authenticator,
										"hint.invalidRealm"));
					}

					break;
				}
				case AUTHENTICATION_SUCCESS: {
					try {
						success = true;

						state.nextModule();

						if (state.isAuthenticationComplete()) {

							permissionService.verifyPermission(
									state.getPrincipal(),
									PermissionStrategy.INCLUDE_IMPLIED,
									AuthenticationPermission.LOGON,
									SystemPermission.SYSTEM_ADMINISTRATION);

							if (!realmService.verifyPrincipal(state
									.getPrincipal())) {

								eventService
										.publishEvent(new AuthenticationAttemptEvent(
												this, state, authenticator,
												"hint.accountSuspended"));

								// Principal is currently suspended from logon
								state.setLastErrorMsg("error.accountSuspended");
								state.setLastErrorIsResourceKey(true);
								state.revertModule();
								success = false;
							} else {

								eventService
										.publishEvent(new AuthenticationAttemptEvent(
												this, state, authenticator));

								for (PostAuthenticationStep proc : postAuthenticationSteps) {
									if (proc.requiresProcessing(state)) {
										state.addPostAuthenticationStep(proc);
									}
								}

								if (state.canCreateSession()) {
									state.setSession(completeLogon(state));
								}

								if (state.hasPostAuthenticationStep()) {
									PostAuthenticationStep step = state
											.getCurrentPostAuthenticationStep();
									if (!step.requiresUserInput(state)) {
										success = logon(state, parameterMap);
									}
								}
							}
						}
					} catch (ResourceException e) {

						log.error("Authentication Failed", e);
						
						eventService
								.publishEvent(new AuthenticationAttemptEvent(
										this, state, authenticator,
										"hint.internalError"));
						// user does not have LOGON permission
						state.setLastErrorMsg("error.noLogonPermission");
						state.setLastErrorIsResourceKey(true);
						state.revertModule();
						success = false;
					} catch (AccessDeniedException e) {

						eventService
								.publishEvent(new AuthenticationAttemptEvent(
										this, state, authenticator,
										"hint.noPermission"));
						// user does not have LOGON permission
						state.setLastErrorMsg("error.noLogonPermission");
						state.setLastErrorIsResourceKey(true);
						state.revertModule();
						success = false;
					}
					break;
				}
				}
			}

			state.authAttempted();

		}

		return success;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public FormTemplate nextAuthenticationTemplate(AuthenticationState state,
			Map params) {
		if (!authenticators.containsKey(state.getCurrentModule().getTemplate())) {
			if (log.isErrorEnabled()) {
				log.error(state.getCurrentModule().getTemplate()
						+ " is not a valid authentication template");
			}
			throw new IllegalStateException(state.getCurrentModule()
					.getTemplate() + " is not a valid authenticator");
		}
		return modifyTemplate(state,
				authenticators.get(state.getCurrentModule().getTemplate())
						.createTemplate(state, params));
	}

	protected FormTemplate modifyTemplate(AuthenticationState state,
			FormTemplate template) {

		for (AuthenticationServiceListener l : listeners) {
			l.modifyTemplate(state, template);
		}
		return template;
	}

	@Override
	public Session completeLogon(AuthenticationState state)
			throws AccessDeniedException {

		if (state.getPrincipal() == null) {
			throw new IllegalStateException(
					"completeLogon called without a principal in the AuthenticationState!");
		}

		Session session = sessionService.openSession(state.getRemoteAddress(),
				state.getPrincipal(), state.getScheme(), state.getUserAgent(),
				state.getParameters());

		setCurrentSession(session, state.getLocale());

		if (state.hasParameter("password")) {
			sessionService.setCurrentPassword(session,
					state.getParameter("password"));
		}
		
		if(permissionService.hasSystemPermission(getCurrentPrincipal())) {
			if(!realmService.getDefaultRealm().equals(getCurrentPrincipal().getRealm())) {
				sessionService.switchRealm(session, realmService.getDefaultRealm());
			}
		} else {
			String altHomePage = configurationService.getValue("session.altHomePage");
			if(StringUtils.isNotBlank(altHomePage)) {
				altHomePage = variableReplacement.replaceVariables(getCurrentPrincipal(), altHomePage);
				state.setHomePage(altHomePage);
			}			
		}
		
		return session;
	}

	@Override
	protected void verifyPermission(Principal principal,
			PermissionStrategy strategy, PermissionType... permissions)
			throws AccessDeniedException {
		permissionService.verifyPermission(principal, strategy, permissions);
	}

	@Override
	public FormTemplate nextPostAuthenticationStep(AuthenticationState state) {

		if (!state.hasPostAuthenticationStep()) {
			throw new IllegalStateException(
					"There are no post authentcation steps to process!");
		}
		return modifyTemplate(state,
				state.getCurrentPostAuthenticationStep().createTemplate(state));
	}

	@Override
	public void registerPostAuthenticationStep(
			PostAuthenticationStep postAuthenticationStep) {
		postAuthenticationSteps.add(postAuthenticationStep);

		Collections.sort(postAuthenticationSteps,
				new Comparator<PostAuthenticationStep>() {
					@Override
					public int compare(PostAuthenticationStep o1,
							PostAuthenticationStep o2) {
						return new Integer(o1.getOrderPriority()).compareTo(o2
								.getOrderPriority());
					}
				});
	}

	@Override
	public Map<String, Authenticator> getAuthenticators(String schemeResourceKey) {
		Map<String, Authenticator> tmp = new HashMap<String, Authenticator>();
		AuthenticationScheme scheme = schemeRepository.getSchemeByResourceKey(
				getCurrentRealm(), schemeResourceKey);

		for (Authenticator a : authenticators.values()) {

			if(a.isHidden()) {
				continue;
			}
			if (scheme.getType() != AuthenticationModuleType.CUSTOM) {
				if (scheme.getType().ordinal() >= a.getType().ordinal()) {
					tmp.put(a.getResourceKey(), a);
				}
			} else {
				if (scheme.getAllowedModules() == null) {
					throw new IllegalStateException(
							"CUSTOM authentication scheme type must declare allowed modules");
				}
				for (String s : scheme.getAllowedModules().split(",")) {
					if (a.getResourceKey().matches(s)) {
						tmp.put(a.getResourceKey(), a);
						break;
					}
				}
			}
		}
		return tmp;
	}

	@Override
	public Principal resolvePrincipalAndRealm(AuthenticationState state,
			String username) throws AccessDeniedException,
			PrincipalNotFoundException {

		// Set before we manipulate it
		state.setLastPrincipalName(username);

		Realm hostRealm = realmService.getRealmByHost((String) state
				.getEnvironmentVariable(BrowserEnvironment.HOST.toString()),
				null);

		String realmName = null;
		if (hostRealm != null) {
			realmName = hostRealm.getName();
			state.setLastRealmName(realmName);
		}

		if(log.isDebugEnabled()) {
			log.debug("Looking up principal " + username + " in " + (hostRealm==null ? "all realms" : hostRealm.getName()));
		}
		
		Principal principal = realmService.getPrincipalByName(hostRealm,
				username, PrincipalType.USER);

		if (principal == null) {

			if(log.isDebugEnabled()) {
				log.debug("Unable to find principal for " + username + " in " + (hostRealm==null ? "all realms" : hostRealm.getName()));
			}
			
			if (!realmService.isRealmStrictedToHost(hostRealm)) {

				hostRealm = null;
				
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
					state.setLastRealmName(realmName);
					hostRealm = realmService.getRealmByName(realmName);
				}

				principal = realmService.getPrincipalByName(hostRealm,
						username, PrincipalType.USER);
			}

			if (principal == null) {
				if(log.isDebugEnabled()) {
					log.debug("Still unable to find principal for " + username + " in " + (hostRealm==null ? "all realms" : hostRealm.getName()));
				}
				throw new PrincipalNotFoundException();
			}
		}

		state.setRealm(principal.getRealm());
		state.setPrincipal(principal);

		return principal;
	}

	@Override
	public Session logonAnonymous(String remoteAddress, String userAgent,
			Map<String, String> parameters, String serverName) throws AccessDeniedException {

		Session session = sessionService.openSession(remoteAddress,
				realmService.getSystemPrincipal(), schemeRepository
						.getSchemeByResourceKey(realmService.getSystemRealm(),
								ANONYMOUS_AUTHENTICATION_RESOURCE_KEY),
				userAgent, parameters, realmService.getRealmByHost(serverName));
		return session;
	}

	@Override
	public Collection<PostAuthenticationStep> getPostAuthenticationSteps() {
		return new ArrayList<PostAuthenticationStep>(postAuthenticationSteps);
	}

}
