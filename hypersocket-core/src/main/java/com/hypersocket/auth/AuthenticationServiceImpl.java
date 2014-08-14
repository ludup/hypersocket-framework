/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

@Service
@Transactional
public class AuthenticationServiceImpl extends AbstractAuthenticatedService
		implements AuthenticationService {

	public static final String DEFAULT_AUTHENTICATION_SCHEME = "Default";

	public static final String DEFAULT_AUTHENTICATION_RESOURCE_KEY = "basic";
	
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
	RealmService realmService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService;

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
				AuthenticationPermission.LOGON.toString(), AuthenticationPermission.LOGON.isSystem(), authentication);

		eventService.registerEvent(AuthenticationEvent.class, RESOURCE_BUNDLE);

		i18nService.registerBundle(RESOURCE_BUNDLE);
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
	public AuthenticationScheme getDefaultScheme(String remoteAddress,
			Map<String, String> environment, Realm realm) {
		List<AuthenticationScheme> schemes = schemeRepository.allSchemes();
		if (schemes.size() == 0)
			throw new IllegalArgumentException(
					"There are no authentication schemes configured!");
		return schemes.get(0);
	}

	@Override
	public boolean isAuthenticatorInScheme(String schemeResourceKey, String resourceKey) {
		
		AuthenticationScheme s = schemeRepository.getSchemeByResourceKey(schemeResourceKey);
		for(AuthenticationModule m : repository.getAuthenticationModulesByScheme(s)) {
			if(m.getTemplate().equals(resourceKey)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public AuthenticationScheme getSchemeByResourceKey(String resourceKey) {
		return schemeRepository.getSchemeByResourceKey(resourceKey);
	}

	@Override
	public AuthenticationScheme getSchemeByName(String name) {
		return schemeRepository.getSchemeByName(name);
	}
	
	@Override
	public AuthenticationState createAuthenticationState(String resourceKey,
			String remoteAddress, Map<String, Object> environment, Locale locale)
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

		state.setScheme(getSchemeByResourceKey(resourceKey));
		state.setModules(repository.getModulesForScheme(state.getScheme()));

		return state;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException {

		state.setLastErrorMsg(null);
		state.setLastErrorIsResourceKey(false);

		boolean success = false;
		
		if (state.isAuthenticationComplete()) {

			if (state.getCurrentPostAuthenticationStep() == null) {
				state.setSession(completeLogon(state));
			} else {
				switch (state.getCurrentPostAuthenticationStep().process(state,
						parameterMap)) {
				case INSUFFICIENT_DATA: {
					state.setLastErrorMsg("error.insufficentData");
					state.setLastErrorIsResourceKey(true);
					break;
				}
				case AUTHENTICATION_SUCCESS: {

					state.nextPostAuthenticationStep();
					success = true;
					if (!state.hasPostAuthenticationStep()) {
						state.setSession(completeLogon(state));
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
		} else {
			Authenticator authenticator = authenticators.get(state
					.getCurrentModule().getTemplate());

			if (authenticator.isSecretModule()
					&& state.getPrincipal() instanceof AuthenticationState.FakePrincipal) {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationEvent(this, state,
						authenticator, "hint.invalidPrincipal"));
			} else {
				switch (authenticator.authenticate(state, parameterMap)) {
				case INSUFFICIENT_DATA: {
					if (state.getAttempts() >= 1) {
						state.setLastErrorMsg("error.insufficentData");
						state.setLastErrorIsResourceKey(true);
					}
					break;
				}
				case AUTHENTICATION_FAILURE_INVALID_CREDENTIALS: {

					if (!authenticator.isSecretModule() && state.hasNextStep()) {
						state.fakeCredentials();
						state.nextModule();
					} else {
						state.setLastErrorMsg("error.genericLogonError");
						state.setLastErrorIsResourceKey(true);
						eventService.publishEvent(new AuthenticationEvent(this,
								state, authenticator, "hint.badCredentials"));
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
						eventService.publishEvent(new AuthenticationEvent(this,
								state, authenticator, "hint.invalidPrincipal"));
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
						eventService.publishEvent(new AuthenticationEvent(this,
								state, authenticator, "hint.invalidRealm"));
					}

					break;
				}
				case AUTHENTICATION_SUCCESS: {
					try {
						permissionService.verifyPermission(
								state.getPrincipal(),
								PermissionStrategy.REQUIRE_ANY,
								AuthenticationPermission.LOGON,
								SystemPermission.SYSTEM_ADMINISTRATION);

						eventService.publishEvent(new AuthenticationEvent(this,
								state, authenticator));

						success = true;
						
						state.nextModule();

						if (state.isAuthenticationComplete()) {

							for (PostAuthenticationStep proc : postAuthenticationSteps) {
								if (proc.requiresProcessing(state)) {
									state.addPostAuthenticationStep(proc);
								}
							}
							if (!state.hasPostAuthenticationStep()) {
								state.setSession(completeLogon(state));
							}
						}
					} catch (AccessDeniedException e) {

						eventService.publishEvent(new AuthenticationEvent(this,
								state, authenticator, "hint.noPermission"));
						// user does not have LOGON permission
						state.setLastErrorMsg("error.noLogonPermission");
						state.setLastErrorIsResourceKey(true);
					}
					break;
				}
				}
			}

			if (!state.isAuthenticationComplete())
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
				state.getPrincipal(), state.getScheme(), state.getUserAgent());

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

		return state.getCurrentPostAuthenticationStep().createTemplate(state);
	}

	@Override
	public void registerPostAuthenticationStep(
			PostAuthenticationStep postAuthenticationStep) {
		postAuthenticationSteps.add(postAuthenticationStep);
	}

	@Override
	public Map<String, Authenticator> getAuthenticators(String resourceKey) {
		Map<String, Authenticator> tmp = new HashMap<String, Authenticator>();
		for(Authenticator a : authenticators.values()) {
			for(String s : a.getAllowedSchemes()) {
				if(resourceKey.matches(s)) {
					tmp.put(a.getResourceKey(), a);
					break;
				}
			}
		}
		return tmp;
	}

}
