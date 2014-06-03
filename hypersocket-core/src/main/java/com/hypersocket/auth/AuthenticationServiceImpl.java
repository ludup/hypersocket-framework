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
import com.hypersocket.menus.MenuRegistration;
import com.hypersocket.menus.MenuService;
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
	public static final String HTTP_AUTHENTICATION_SCHEME = "HTTP";

	private static Logger log = LoggerFactory
			.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	PermissionService permissionService;

	@Autowired
	AuthenticationRepository repository;

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

	@Autowired
	MenuService menuService;

	// @Autowired
	// IndexPageFilter indexPageFilter;
	Map<String, Authenticator> authenticators = new HashMap<String, Authenticator>();

	List<PostAuthenticationStep> postAuthenticationSteps = new ArrayList<PostAuthenticationStep>();

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
				AuthenticationPermission.LOGON.toString(), authentication);

		eventService.registerEvent(AuthenticationEvent.class, RESOURCE_BUNDLE);

		menuService.registerMenu(new MenuRegistration(RESOURCE_BUNDLE,
				"authentication", "fa-authentication", "authentication", 1,
				SystemPermission.SYSTEM_ADMINISTRATION,
				SystemPermission.SYSTEM_ADMINISTRATION,
				SystemPermission.SYSTEM_ADMINISTRATION,
				SystemPermission.SYSTEM_ADMINISTRATION), "system");

		i18nService.registerBundle(RESOURCE_BUNDLE);

		// indexPageFilter.addStyleSheet("${uiPath}/css/authenticator.css");
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
		List<AuthenticationScheme> schemes = repository.allSchemes();
		if (schemes.size() == 0)
			throw new IllegalArgumentException(
					"There are no authentication schemes configured!");
		return schemes.get(0);
	}

	public AuthenticationScheme getAuthenticationScheme(String scheme) {
		return repository.getScheme(scheme);
	}

	@Override
	public AuthenticationState createAuthenticationState(String scheme,
			String remoteAddress, Map<String, String> environment, Locale locale)
			throws AccessDeniedException {

		AuthenticationState state = new AuthenticationState(remoteAddress,
				locale, environment);

		// Can we determine the principal from the current information
		if (environment
				.containsKey(BrowserEnvironment.AUTHORIZATION.toString())) {

			String header = environment.get(BrowserEnvironment.AUTHORIZATION
					.toString());
			if (header.toLowerCase().startsWith("basic")) {
				header = new String(Base64.decode(header.substring(6)));
				int idx = header.indexOf(':');
				if (idx > -1) {
					String username = header.substring(0, idx);
					String password = header.substring(idx + 1);
					if ((idx = username.indexOf('@')) > -1) {
						state.addParameter(
								UsernameAndPasswordTemplate.REALM_FIELD,
								username.substring(idx + 1));
						username = username.substring(0, idx);
					} else if ((idx = username.indexOf('\\')) > -1) {
						state.addParameter(
								UsernameAndPasswordTemplate.REALM_FIELD,
								username.substring(0, idx));
						username = username.substring(idx + 1);
					}
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
				String hostHeader = environment.get(BrowserEnvironment.HOST
						.toString());
				int idx;
				if ((idx = hostHeader.indexOf(':')) > -1) {
					hostHeader = hostHeader.substring(0, idx);
				}

				state.setRealm(realmService.getRealmByHost(hostHeader));
			}
		}

		state.setScheme(getAuthenticationScheme(scheme));
		state.setModules(repository.getModulesForScheme(state.getScheme()));

		return state;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void logon(AuthenticationState state, Map parameterMap)
			throws AccessDeniedException {

		state.setLastErrorMsg(null);
		state.setLastErrorIsResourceKey(false);

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
			switch (authenticator.authenticate(state, parameterMap)) {
			case INSUFFICIENT_DATA: {
				if (!state.isNew()) {
					state.setLastErrorMsg("error.insufficentData");
					state.setLastErrorIsResourceKey(true);
				}
				break;
			}
			case AUTHENTICATION_FAILURE_INVALID_CREDENTIALS: {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationEvent(this, state,
						authenticator, "hint.badCredentials"));
				break;
			}
			case AUTHENTICATION_FAILURE_INVALID_PRINCIPAL: {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationEvent(this, state,
						authenticator, "hint.invalidPrincipal"));
			}
			case AUTHENTICATION_FAILURE_INVALID_REALM: {
				state.setLastErrorMsg("error.genericLogonError");
				state.setLastErrorIsResourceKey(true);
				eventService.publishEvent(new AuthenticationEvent(this, state,
						authenticator, "hint.invalidRealm"));
			}
			case AUTHENTICATION_SUCCESS: {
				try {
					permissionService.verifyPermission(state.getPrincipal(),
							PermissionStrategy.REQUIRE_ANY,
							AuthenticationPermission.LOGON,
							SystemPermission.SYSTEM_ADMINISTRATION);

					eventService.publishEvent(new AuthenticationEvent(this,
							state, authenticator));
					state.setCurrentIndex(state.getCurrentIndex() + 1);

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

			if (!state.isNew() && !state.isAuthenticationComplete())
				state.attempts++;

			state.isNew = false;
		}
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
		return authenticators.get(state.getCurrentModule().getTemplate())
				.createTemplate(state, params);
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
			ChangePasswordAuthenticationStep postAuthenticationStep) {
		postAuthenticationSteps.add(postAuthenticationStep);
	}

	// @Override
	// public boolean logon(String username, char[] password,
	// InetSocketAddress remoteAddress) throws AccessDeniedException {
	//
	// String realm = "";
	// int idx = username.indexOf('\\');
	// if (idx > -1) {
	// realm = username.substring(0, idx);
	// username = username.substring(idx + 1);
	// }
	//
	// Realm r = realmService.getRealmByName(realm);
	// Principal principal = realmService.getPrincipalByName(r, username,
	// PrincipalType.USER);
	//
	// boolean success = realmService.verifyPassword(principal, password);
	//
	// verifyPermission(principal, PermissionStrategy.REQUIRE_ANY,
	// SystemPermission.SYSTEM_ADMINISTRATION,
	// AuthenticationPermission.LOGON);
	//
	// sessionService.openSession(remoteAddress.getAddress()
	// .getHostAddress(), principal, null);
	//
	// return success;
	// }

	public List<AuthenticationScheme> getAuthenticationSchemes()
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.READ);
		return repository.getAuthenticationSchemes();
	}

	public List<AuthenticationModule> getAuthenticationModules()
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.READ);
		return repository.getAuthenticationModules();
	}

	public List<AuthenticationModule> getAuthenticationModulesByScheme(
			AuthenticationScheme authenticationScheme)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.READ);
		return repository
				.getAuthenticationModulesByScheme(authenticationScheme);
	}

	public List<Authenticator> getRegAuthenticators() {

		return new ArrayList<Authenticator>(authenticators.values());
	}

	public AuthenticationModule getModuleById(Long id)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.READ);
		return repository.getModuleById(id);
	}

	public AuthenticationScheme getSchemeById(Long id)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.READ);
		return repository.getSchemeById(id);
	}

	public void updateSchemeModules(List<AuthenticationModule> moduleList)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.CREATE,
				AuthenticationPermission.DELETE);

		deleteModulesByScheme(moduleList.get(0).getScheme());
		for (AuthenticationModule module : moduleList) {

			repository.createAuthenticationModule(module);

		}
	}

	public AuthenticationModule createAuthenticationModule(
			AuthenticationModule authenticationModule)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.CREATE);

		return repository.createAuthenticationModule(authenticationModule);
	}

	public AuthenticationModule updateAuthenticationModule(
			AuthenticationModule authenticationModule)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.UPDATE);

		return repository.updateAuthenticationModule(authenticationModule);
	}

	public void deleteModule(AuthenticationModule authenticationModule)
			throws AccessDeniedException {
		assertPermission(AuthenticationPermission.DELETE);

		repository.deleteModule(authenticationModule);
	}

	public void deleteModulesByScheme(AuthenticationScheme authenticationScheme)
			throws AccessDeniedException {

		List<AuthenticationModule> list = getAuthenticationModulesByScheme(authenticationScheme);
		for (AuthenticationModule module : list) {
			repository.deleteModule(module);
		}

	}
}
