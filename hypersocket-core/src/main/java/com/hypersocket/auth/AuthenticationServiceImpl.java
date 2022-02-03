/*******************************************************************************
] * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserVariableReplacementService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

@Service
public class AuthenticationServiceImpl extends
		PasswordEnabledAuthenticatedServiceImpl implements
		AuthenticationService {

	public static final String BASIC_AUTHENTICATION_SCHEME = "Basic";
	public static final String BASIC_AUTHENTICATION_RESOURCE_KEY = "basic";

	public static final String ANONYMOUS_AUTHENTICATION_SCHEME = "Anonymous";
	public static final String ANONYMOUS_AUTHENTICATION_RESOURCE_KEY = "anonymous";

	public static final String AUTHENTICATION_SCHEME_USER_LOGIN_RESOURCE_KEY = "userLogin";
	public static final String AUTHENTICATION_SCHEME_NAME = "User Login";
	
	public static final String FALLBACK_AUTHENTICATION_RESOURCE_KEY = "fallback";
	
	private static Logger log = LoggerFactory
			.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private AuthenticationModuleRepository repository;

	@Autowired
	private AuthenticationSchemeRepository schemeRepository;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private UserVariableReplacementService variableReplacement;
		
	private Map<String, Authenticator> authenticators = new HashMap<String, Authenticator>();
	private List<PostAuthenticationStep> postAuthenticationSteps = new ArrayList<PostAuthenticationStep>();
	private List<AuthenticationServiceListener> listeners = new ArrayList<AuthenticationServiceListener>();
	private AuthenticationSchemeSelector authenticationSelector;
	private List<AuthenticatorSelector> authenticatorSelectors = new ArrayList<AuthenticatorSelector>();
	
	@PostConstruct
	private void postConstruct() {

		if (log.isInfoEnabled()) {
			log.info("Configuring Authentication Service");
		}

		PermissionCategory authentication = permissionService
				.registerPermissionCategory(RESOURCE_BUNDLE,
						"category.authentication");

		permissionService.registerPermission(
				AuthenticationPermission.LOGON, authentication);

		eventService.registerEvent(AuthenticationAttemptEvent.class,
				RESOURCE_BUNDLE);

		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		schemeRepository.registerAuthenticationScheme(AuthenticationServiceImpl.BASIC_AUTHENTICATION_RESOURCE_KEY);
		schemeRepository.registerAuthenticationScheme(AuthenticationServiceImpl.AUTHENTICATION_SCHEME_USER_LOGIN_RESOURCE_KEY);

		setupRealms();
		setupFallback();
		
	}
	
	private void setupFallback() {

		realmService.registerRealmListener(new RealmAdapter() {

			public boolean hasCreatedDefaultResources(Realm realm) {
				return schemeRepository.getSchemeByResourceKeyCount(realm,
						FALLBACK_AUTHENTICATION_RESOURCE_KEY) > 0;
			}

			public void onCreateRealm(Realm realm) {

				if (log.isInfoEnabled()) {
					log.info("Creating unlicensed authentication scheme for realm "
							+ realm.getName());
				}

				List<String> modules = new ArrayList<String>();
				modules.add(FallbackAuthenticator.RESOURCE_KEY);

				schemeRepository.createScheme(realm, "fallback", modules,
						"fallback", true, 1, AuthenticationModuleType.BASIC, false);

			}
		});

	}

	private void setupRealms() {

		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return schemeRepository.getSchemeByResourceKeyCount(realm,
						BASIC_AUTHENTICATION_RESOURCE_KEY) > 0;
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
						AuthenticationModuleType.HIDDEN, false);

				if (log.isInfoEnabled()) {
					log.info("Creating " + BASIC_AUTHENTICATION_SCHEME
							+ " authentication scheme for realm "
							+ realm.getName());
				}

				modules.add(UsernameAndPasswordAuthenticator.RESOURCE_KEY);
				schemeRepository.createScheme(realm,
						BASIC_AUTHENTICATION_SCHEME, modules,
						BASIC_AUTHENTICATION_RESOURCE_KEY, false, 10,
						AuthenticationModuleType.HTML, false);

			}
			
			@Override
			public Integer getWeight() {
				return Integer.MIN_VALUE + 1000;
			}
		});

		realmService.registerRealmListener(new RealmAdapter() {
			public boolean hasCreatedDefaultResources(Realm realm) {
				return schemeRepository.getSchemeByResourceKeyCount(realm,
							AUTHENTICATION_SCHEME_USER_LOGIN_RESOURCE_KEY) > 0;
			}
			
			public void onCreateRealm(Realm realm) {

				if (log.isInfoEnabled()) {
					log.info("Creating " + AUTHENTICATION_SCHEME_NAME
							+ " authentication scheme for realm "
							+ realm.getName());
				}
				
				AuthenticationScheme basicScheme = schemeRepository.getSchemeByResourceKey2(realm, AuthenticationServiceImpl.BASIC_AUTHENTICATION_RESOURCE_KEY);
				List<String> modules = new ArrayList<String>();
				modules.add(UsernameAndPasswordAuthenticator.RESOURCE_KEY);
				
				if (log.isInfoEnabled()) {
					log.info("Creating " + AUTHENTICATION_SCHEME_NAME
							+ " authentication scheme for realm "
							+ realm.getName());
				}
				
				AuthenticationScheme userLoginScheme = schemeRepository.createScheme(realm,
						AUTHENTICATION_SCHEME_NAME, modules,
						AUTHENTICATION_SCHEME_USER_LOGIN_RESOURCE_KEY,
						false, 
						10, 
						AuthenticationModuleType.HTML,
						true);
				userLoginScheme.setSystem(true);
				userLoginScheme.setSupportsHomeRedirect(true);
			
				try {
					schemeRepository.saveResource(userLoginScheme);
				} catch (ResourceException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				
				List<AuthenticationModule> basicModules = repository.getModulesForScheme(basicScheme);
				AuthenticationModule m = basicModules.iterator().next();
				m.setTemplate(UsernameAndPasswordAuthenticator.RESOURCE_KEY);
				m.setIndex(0);
				
				repository.updateAuthenticationModule(m);
				
				if(basicModules.size() > 1) {
					for(int i = 1;i<basicModules.size();i++) {
						repository.deleteModule(basicModules.get(i));
					}
				}
				
				basicScheme.setHidden(true);
				try {
					schemeRepository.saveResource(basicScheme);
				} catch (ResourceException e) {
					log.error("Failed to save authentication scheme", e);
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			
			@Override
			public Integer getWeight() {
				return Integer.MIN_VALUE + 1001;
			}
		});
		
		
	}

	@Override
	public void registerListener(AuthenticationServiceListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void registerAuthenticatorSelector(AuthenticatorSelector selector) {
		authenticatorSelectors.add(selector);
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
			Map<String, Object> environment, Realm realm) {
		AuthenticationScheme scheme = schemeRepository.getSchemeByResourceKey2(realm, BASIC_AUTHENTICATION_RESOURCE_KEY);
		if(scheme!=null) {
			return scheme;
		}
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
		return getSchemeByResourceKey(realm, resourceKey, true);
	}
	
	@Override
	public AuthenticationScheme getSchemeByResourceKey(Realm realm,
			String resourceKey, boolean allowFallback) throws AccessDeniedException {
		if(allowFallback) {
			return schemeRepository.getSchemeByResourceKey(realm, resourceKey);
		} else {
			return schemeRepository.getSchemeByResourceKey2(realm, resourceKey);
		}
	}

	@Override
	public AuthenticationState createAuthenticationState(
			String schemeResourceKey, String remoteAddress,
			Map<String, Object> environment, Realm realm, Locale locale)
			throws AccessDeniedException {

		AuthenticationState state = new AuthenticationState(remoteAddress, environment, locale);

		if(realm==null) {
			realm = realmService.getDefaultRealm();
		}
		
		state.setRealm(realm);
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Creating authentication state for %s realm", realm==null ? "unknown" : realm.getName()));
		}
		
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

		AuthenticationScheme scheme = selectScheme(schemeResourceKey, state);
		
		if(scheme==null) {
			if(realm==null) {
				realm = realmService.getDefaultRealm();
			}
			if(schemeResourceKey!=null) {
				scheme = getSchemeByResourceKey(
						realm,
						schemeResourceKey);
			} else {
				scheme = getDefaultScheme(remoteAddress, environment, realm);
			}
			
		}

		if (scheme == null) {
			if (log.isWarnEnabled()) {
				log.warn(schemeResourceKey
						+ " is not a valid authentication scheme");
			}
			scheme = getSchemeByResourceKey(
					state.getRealm(),
					AuthenticationServiceImpl.BASIC_AUTHENTICATION_RESOURCE_KEY);
		}
		
		state.setInitialScheme(scheme);
		
		List<AuthenticationModule> modulesForScheme = repository.getModulesForScheme(scheme);
		if(modulesForScheme.isEmpty())
			throw new AccessDeniedException(String.format("No authentication modules for the scheme %s.", scheme.getName()));
		
		state.setScheme(scheme);
		state.setModules(modulesForScheme);

		return state;
	}

	private AuthenticationScheme selectScheme(String schemeResourceKey, AuthenticationState state) throws AccessDeniedException {
		if(authenticationSelector!=null) {
			return authenticationSelector.selectScheme(schemeResourceKey, state);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException, FallbackAuthenticationRequired {

		boolean success = false;

		if (state.isAuthenticationComplete()) {

			boolean clearContext = false;
			if (state.getSession() != null) {
				setCurrentSession(state.getSession(), state.getLocale());
				clearContext = true;
			}

			try {
				if (state.getCurrentPostAuthenticationStep() != null) {

					preProcess(state.getCurrentPostAuthenticationStep(), state, parameterMap);
					
					AuthenticatorResult result = state.getCurrentPostAuthenticationStep().process(state, parameterMap);
					
					postProcess(state.getCurrentPostAuthenticationStep(), result, state, parameterMap);
					
					switch (result) {
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
					case AUTHENTICATION_FAILURE_DISPALY_ERROR: 
					{
						break;
					}
					case AUTHENTICATION_SUCCESS: {

						
						success = true;
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
				if(clearContext) {
					clearPrincipalContext();
				}
			}
		} else {
			Authenticator authenticator = nextAuthenticator(state);
			Realm currentRealm = state.getRealm(); // The default realm
			if (authenticator == null) {
				throw new FallbackAuthenticationRequired();
			}

			if (authenticator.isSecretModule()
					&& state.getPrincipal() instanceof FakePrincipal) {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationAttemptEvent(this,
						state, authenticator, "hint.invalidPrincipal"));
			} else {
				
				if(checkSuspensions(state, authenticator)) {
				
					AuthenticatorResult result;
					if(state.getPrincipal()!=null && state.getPrincipal() instanceof FakePrincipal) {
						result = AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_PRINCIPAL;
					} else {
						preProcess(authenticator, state, parameterMap);
						
						result = authenticator.authenticate(state, parameterMap);
						
						postProcess(authenticator, result, state, parameterMap);
					}
					
					switch (result) {
					case INSUFFICIENT_DATA: {
						if (state.getLastErrorMsg() == null && parameterMap.size() > 1) {
							if (state.getAttempts() >= 1) {
								state.setLastErrorMsg("error.insufficentData");
								state.setLastErrorIsResourceKey(true);
							}
						}
						break;
					}
					case INSUFFICIENT_DATA_NO_ERROR: {
						state.setLastErrorMsg(null);
						state.setLastErrorIsResourceKey(false);
						break;
					}
					case AUTHENTICATION_FAILURE_DISPALY_ERROR: 
					{
						if (authenticator.isIdentityModule() && !authenticator.isSecretModule() && state.hasNextStep()) {
							state.fakeCredentials();
							state.nextModule();
						} else {
							eventService
									.publishEvent(new AuthenticationAttemptEvent(
											this, state, authenticator, false));
						}
						break;
					}
					case AUTHENTICATION_FAILURE_INVALID_CREDENTIALS: {
	
						if (authenticator.isIdentityModule() && !authenticator.isSecretModule() && state.hasNextStep()) {
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
	
						if (authenticator.isIdentityModule() && !authenticator.isSecretModule() && state.hasNextStep()) {
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
	
						if (authenticator.isIdentityModule() && !authenticator.isSecretModule() && state.hasNextStep()) {
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
					case AUTHENTICATION_SUCCESS: 
					case AUTHENTICATION_SWITCHED: {
						try {
							success = true;
							
							state.setLastErrorMsg(null);
							state.setLastErrorIsResourceKey(false);
							
							if((currentRealm==null || !state.getRealm().equals(currentRealm)) && state.getCurrentIndex()==0) {
								/**
								 * The users realm is not the realm we started off in. We need to switch
								 */
								AuthenticationScheme realmScheme = schemeRepository.getSchemeByResourceKey(
										state.getRealm(), state.getScheme().getResourceKey());
								List<AuthenticationModule> modules = repository.getModulesForScheme(realmScheme);
								if(modules.isEmpty()) {
									throw new IllegalStateException("Incorrect authentication configured. Contact your Administrator");
								}
								if(state.getCurrentModule().getTemplate().equals(modules.get(0).getTemplate())) {
									if(log.isInfoEnabled()) {
										log.info(String.format("Switching realms from %s to %s", currentRealm==null ? "unknown" : currentRealm.getName(), state.getRealm().getName()));
									}
									state.setScheme(realmScheme);
									state.setModules(modules);
								} else {
									throw new IllegalStateException("Invalid realm configuration. Contact your Administrator");
								}
								
							}
							
							// We need to reset the scheme to the new principal realm.
//							state.setScheme(schemeRepository.getSchemeByResourceKey(principal.getRealm(), 
//									state.getScheme().getResourceKey()));
							
							if(state.getPrincipal()!=null) {
								if(!state.getScheme().getAllowedRoles().isEmpty()) {
									boolean found = permissionService.hasRole(state.getPrincipal(), state.getScheme().getAllowedRoles());
									
									if(!found) {
										state.clean();
										state.setLastErrorMsg(StringUtils.isNotBlank(state.getScheme().getDeniedRoleError()) ? state.getScheme().getDeniedRoleError() : "error.roleNotAllowed");
										state.setLastErrorIsResourceKey(true);
										
										success = false;
										break;
									}
								}
								
								if(!state.getScheme().getDeniedRoles().isEmpty()) {
									boolean found = permissionService.hasRole(state.getPrincipal(), state.getScheme().getDeniedRoles());
									
									if(found) {
										state.clean();
										state.setLastErrorMsg(StringUtils.isNotBlank(state.getScheme().getDeniedRoleError()) ? state.getScheme().getDeniedRoleError() : "error.roleDenied");
										state.setLastErrorIsResourceKey(true);
										
										success = false;
										break;
									}
								}
							}
							
							if(result==AuthenticatorResult.AUTHENTICATION_SUCCESS) {
								state.nextModule();
							}
	
							if(!state.isPrimaryState() && state.isAuthenticationComplete()) {
								state.switchBack();
							}
							
							if (state.isAuthenticationComplete()) {
	
								permissionService.verifyPermission(
										state.getRealm(),
										state.getPrincipal(),
										PermissionStrategy.INCLUDE_IMPLIED,
										AuthenticationPermission.LOGON,
										SystemPermission.SYSTEM_ADMINISTRATION);
	
								eventService.publishEvent(new AuthenticationAttemptEvent(
												this, state, authenticator));

								setupSystemContext(state.getPrincipal());
								
								try {
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
								} finally {
									clearPrincipalContext();
								}
								
							} else {
								authenticator = nextAuthenticator(state);
								
								if(!authenticator.requiresUserInput(state)) {
									return logon(state, parameterMap);
								}
							}
						} catch(IllegalStateException e) { 
							
							state.setLastErrorMsg(e.getMessage());
							state.setLastErrorIsResourceKey(false);
							state.revertModule();
							success = false;
							
						} catch (AccessDeniedException e) {
	
							log.error("User cannot login", e);
							
							eventService.publishEvent(new AuthenticationAttemptEvent(
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
			}
			state.authAttempted();

		}

		return success;

	}

	private void postProcess(Authenticator authenticator, AuthenticatorResult result, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap) {
		for(AuthenticationServiceListener listener : listeners) {
			try {
				listener.postProcess(authenticator, result, state, parameterMap);
			} catch (Throwable e) {
				log.error("Error post processing authentication", e);
			}
		}
	}

	private void preProcess(Authenticator authenticator, AuthenticationState state, @SuppressWarnings("rawtypes") Map parameterMap) {
		for(AuthenticationServiceListener listener : listeners) {
			try {
				listener.preProcess(authenticator, state, parameterMap);
			} catch (Throwable e) {
				log.error("Error pre processing authentication", e);
			}
		}
	}

	private void preProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticationState state,
			@SuppressWarnings("rawtypes") Map parameterMap) {
		for(AuthenticationServiceListener listener : listeners) {
			try {
				listener.preProcess(currentPostAuthenticationStep, state, parameterMap);
			} catch (Throwable e) {
				log.error("Error pre processing post authentication", e);
			}
		}
	}

	private void postProcess(PostAuthenticationStep currentPostAuthenticationStep, AuthenticatorResult result,
			AuthenticationState state, @SuppressWarnings("rawtypes") Map parameterMap) {
		for(AuthenticationServiceListener listener : listeners) {
			try {
				listener.postProcess(currentPostAuthenticationStep, result, state, parameterMap);
			} catch (Throwable e) {
				log.error("Error post processing post authentication", e);
			}
		}
	}

	@Override 
	public Authenticator nextAuthenticator(AuthenticationState state) {
		Authenticator auth = authenticators.get(state.getCurrentModule().getTemplate());
		if (auth==null) {
			if (log.isErrorEnabled()) {
				log.error(state.getCurrentModule().getTemplate()
						+ " is not a valid authentication template");
			}
			throw new IllegalStateException(state.getCurrentModule()
					.getTemplate() + " is not a valid authenticator");
		}
		
		for(AuthenticatorSelector selector : authenticatorSelectors) {
			if(selector.isAuthenticatorOverridden(state, auth)) {
				auth = selector.selectAuthenticator(state, auth);
			}
		}
		return auth;
	}
	@Override
	@SuppressWarnings("rawtypes")
	public FormTemplate nextAuthenticationTemplate(AuthenticationState state,
			Map params) {
		
		Authenticator nextAuthenticator = nextAuthenticator(state);
		FormTemplate template = nextAuthenticator.createTemplate(state, params);

		return modifyTemplate(state, template, false);
	}

	private boolean checkSuspensions(AuthenticationState state, Authenticator authenticator) {
		
		if(authenticator.isSecretModule()) {
			if (!realmService.verifyPrincipal(state.getLastPrincipalName(), state.getRealm())) {
				state.clean();
				state.setLastErrorMsg("error.accountSuspended");
				state.setLastErrorIsResourceKey(true);
				
				eventService.publishEvent(new AuthenticationAttemptEvent(
						this, state, authenticator,
						"hint.accountSuspended"));
				
				
				return false;
			} 
		}

		return true;
	}
	
	protected FormTemplate modifyTemplate(AuthenticationState state,
			FormTemplate template, boolean authenticated) {

		for (AuthenticationServiceListener l : listeners) {
			l.modifyTemplate(state, template, authenticated);
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

		state.complete(session);
		
		if (state.hasParameter("password")) {
			sessionService.setCurrentPassword(session,
					state.getParameter("password"));
		}

		if (permissionService.hasSystemPermission(getCurrentPrincipal())) {
			if (!realmService.getDefaultRealm().equals(
					getCurrentPrincipal().getRealm())) {
				sessionService.switchRealm(session,
						realmService.getDefaultRealm());
			}
		} 
		
		String altHomePage = configurationService.getValue(state.getRealm(), "session.altHomePage");
		if(StringUtils.isNotBlank(altHomePage)) {
			
			List<String> schemes = Arrays.asList(
					configurationService.getValues(state.getRealm(), "session.altHomePage.onSchemes"));
			
			if(schemes.contains(state.getScheme().getResourceKey())) {
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
	public FormTemplate nextPostAuthenticationStep(AuthenticationState state) throws AccessDeniedException {

		if (!state.hasPostAuthenticationStep()) {
			throw new IllegalStateException(
					"There are no post authentcation steps to process!");
		}
		FormTemplate t = modifyTemplate(state, state.getCurrentPostAuthenticationStep().createTemplate(state), true);
		return t;
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
						return Integer.valueOf(o1.getOrderPriority()).compareTo(o2
								.getOrderPriority());
					}
				});
	}

	@Override
	public void setAuthenticationSchemeSelector(AuthenticationSchemeSelector authenticationSelector) {
		if(this.authenticationSelector!=null) {
			throw new IllegalStateException("You cannot set a scheme selector when its already set!");
		}
		this.authenticationSelector = authenticationSelector;
	}
	
	@Override
	public Map<String, Authenticator> getAuthenticators(String schemeResourceKey) {
		Map<String, Authenticator> tmp = new HashMap<String, Authenticator>();
		AuthenticationScheme scheme = schemeRepository.getSchemeByResourceKey2(
				getCurrentRealm(), schemeResourceKey);

		AuthenticationModuleType type = Objects.isNull(scheme) ? AuthenticationModuleType.HTML : scheme.getType(); 
		
		for (Authenticator a : authenticators.values()) {

			if (a.isHidden() || !a.isEnabled()) {
				continue;
			}
			if (type != AuthenticationModuleType.CUSTOM) {
				if (type.ordinal() >= a.getType().ordinal()) {
					tmp.put(a.getResourceKey(), a);
				}
			}  else {
				tmp.put(a.getResourceKey(), a);
			}
		}
		return tmp;
	}

	@Override
	public Principal resolvePrincipalAndRealm(AuthenticationState state,
			String username,
			Realm selectedRealm, PrincipalType... types) throws AccessDeniedException,
			PrincipalNotFoundException {

		// Set before we manipulate it
		state.setLastPrincipalName(username);

		if(selectedRealm!=null) {
			state.setRealm(selectedRealm);
		}
		
		Realm authRealm = state.getRealm();
		String realmName = null;
		if (authRealm != null) {
			realmName = authRealm.getName();
			state.setLastRealmName(realmName);
		}

		if (log.isDebugEnabled()) {
			log.debug("Looking up principal " + username + " in "
					+ (authRealm == null ? "all realms" : authRealm.getName()));
		}

		Principal principal = null;
		
		if(realmService.isRealmStrictedToHost(authRealm) || selectedRealm!=null) {
			principal =  realmService.getPrincipalByName(
					authRealm,
					username, types);
		} else {
			try {
				principal = realmService.getUniquePrincipal(username, types);
			} catch (ResourceNotFoundException e) {
			}
		}

		if (principal == null) {

			if (log.isDebugEnabled()) {
				log.debug("Unable to find principal for "
						+ username
						+ " in "
						+ (authRealm == null ? "all realms" : authRealm
								.getName()));
			}

			if(authRealm!=null) {
				if (log.isDebugEnabled()) {
					log.debug("Performing realm direct lookup for principal " + username + " in "
							+ authRealm.getName());
				}
				principal = realmService.getPrincipalByName(authRealm, username, types);
			}
			
			if(principal==null) {
				throw new PrincipalNotFoundException(String.format("%s is not a valid username", username));
			}
			
		}

		state.setLastPrincipal(principal);

		return principal;
	}

	@Override
	public Session logonAnonymous(String remoteAddress, String userAgent,
			Map<String, String> parameters, String serverName)
			throws AccessDeniedException {

		Session session = sessionService.openSession(remoteAddress,
				realmService.getSystemPrincipal(), schemeRepository
						.getSchemeByResourceKey2(realmService.getSystemRealm(),
								ANONYMOUS_AUTHENTICATION_RESOURCE_KEY),
				userAgent, parameters, realmService.getRealmByHost(serverName));
		return session;
	}

	@Override
	public Collection<PostAuthenticationStep> getPostAuthenticationSteps() {
		return new ArrayList<PostAuthenticationStep>(postAuthenticationSteps);
	}

	@Override
	public boolean isAuthenticatorInUse(Realm realm, String resourceKey) {
		return repository.isAuthenticatorInUse(realm, resourceKey);
	}

	@Override
	public <T> T callAs(Callable<T> callable, Principal principal) {
		setupSystemContext(principal);
		try {
			try {
				return callable.call();
			} catch(RuntimeException re) {
				throw re;
			}catch (Exception e) {
				throw new IllegalStateException(String.format("Call as %s failed.", principal.getName()), e);
			}
		}
		finally {
			clearPrincipalContext();
		}
	}

	@Override
	public <T> T callAsSystemContext(Callable<T> callable, Realm realm) {
		setupSystemContext(realm);
		try {
			try {
				return callable.call();
			} catch(RuntimeException re) {
				throw re;
			}catch (Exception e) {
				throw new IllegalStateException(String.format("Call as %s failed.", realm.getName()), e);
			}
		}
		finally {
			clearPrincipalContext();
		}
	}

	@Override
	public void runAsSystemContext(Runnable runnable, Realm realm) {
		setupSystemContext(realm);
		try {
			runnable.run();
		} finally {
			clearPrincipalContext();
		}
	}
}
