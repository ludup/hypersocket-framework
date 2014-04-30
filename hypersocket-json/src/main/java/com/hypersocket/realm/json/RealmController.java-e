/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalColumns;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmColumns;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DataTablesResult;
import com.hypersocket.tables.json.DataTablesPageProcessor;

@Controller
public class RealmController extends ResourceController {

	@AuthenticationRequired
	@RequestMapping(value = "realm/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Realm getRealm(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return realmService.getRealmById(id);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Realm> listRealms(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Realm>(realmService.allRealms());
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "groups", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listGroups(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(realmService.allGroups(
					sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "groups/{user}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listGroups(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long user)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					realmService.getAssociatedPrincipals(realmService
							.getPrincipalById(
									sessionUtils.getCurrentRealm(request),
									user, PrincipalType.USER),
							PrincipalType.GROUP));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "users", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listUsers(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(realmService.allUsers(
					sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "table/realms", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public DataTablesResult tableRealms(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new DataTablesPageProcessor() {

						@Override
						public Column getColumn(int col) {
							return RealmColumns.values()[col];
						}

						@Override
						public List<?> getPage(String searchPattern, int start, int length,
								ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
							return realmService.getRealms(searchPattern, start, length, sorting);
						}
						
						@Override
						public Long getTotalCount(String searchPattern) throws UnauthorizedException, AccessDeniedException {
							return realmService.getRealmCount(searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "table/users", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public DataTablesResult tableUsers(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new DataTablesPageProcessor() {

						@Override
						public Column getColumn(int col) {
							return PrincipalColumns.values()[col];
						}

						@Override
						public List<?> getPage(String searchPattern, int start, int length,
								ColumnSort[] sorting) throws UnauthorizedException {
							return realmService.getPrincipals(sessionUtils.getCurrentRealm(request),
									PrincipalType.USER, searchPattern, start, length, sorting);
						}
						
						@Override
						public Long getTotalCount(String searchPattern) throws UnauthorizedException {
							return realmService.getPrincipalCount(
									sessionUtils.getCurrentRealm(request),
									PrincipalType.USER,
									searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "table/groups", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public DataTablesResult tableGroups(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new DataTablesPageProcessor() {

						@Override
						public Column getColumn(int col) {
							return PrincipalColumns.values()[col];
						}

						@Override
						public List<?> getPage(String searchPattern, int start, int length,
								ColumnSort[] sorting) throws UnauthorizedException {
							return realmService.getPrincipals(sessionUtils.getCurrentRealm(request),
									PrincipalType.GROUP, searchPattern, start, length, sorting);
						}
						
						@Override
						public Long getTotalCount(String searchPattern) throws UnauthorizedException {
							return realmService.getPrincipalCount(
									sessionUtils.getCurrentRealm(request),
									PrincipalType.GROUP,
									searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "users/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listUsers(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					realmService.getAssociatedPrincipals(realmService
							.getPrincipalById(
									sessionUtils.getCurrentRealm(request),
									group, PrincipalType.GROUP),
							PrincipalType.USER));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realm/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRealmTemplate(HttpServletRequest request,
			@PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(realmService.getRealmPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}
		
	}

	@AuthenticationRequired
	@RequestMapping(value = "user/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserTemplate(HttpServletRequest request,
			@PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(realmService.getUserPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}
		
	}

	@AuthenticationRequired
	@RequestMapping(value = "group/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getGroupTemplate(HttpServletRequest request,
			@PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(realmService.getGroupPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}
		
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "realm/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRealmPropertiesJson(HttpServletRequest request,
			@PathVariable("id") Long module) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.getRealmById(module);
			return new ResourceList<PropertyCategory>(
					realmService.getRealmPropertyTemplates(realm));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "user/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserProperties(HttpServletRequest request,
			@PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Principal principal = realmService.getPrincipalById(
					sessionUtils.getCurrentRealm(request), id,
					PrincipalType.USER);
			return new ResourceList<PropertyCategory>(
					realmService.getUserPropertyTemplates(principal));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realm", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Realm> createOrUpdateRealm(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody RealmUpdate realm) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : realm.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			Realm newRealm;

			if (realm.getId() != null) {
				newRealm = realmService.updateRealm(
						realmService.getRealmById(realm.getId()),
						realm.getName(), properties);
			} else {
				newRealm = realmService.createRealm(realm.getName(),
						realm.getType(), properties);
			}
			return new ResourceStatus<Realm>(newRealm, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					realm.getId() != null ? "info.realm.updated"
							: "info.realm.created", realm.getName()));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Realm>(false, e.getMessage());
		} catch (ResourceCreationException e) {
			return new ResourceStatus<Realm>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realm/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Realm> deleteRealm(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				return new ResourceStatus<Realm>(false, I18N.getResource(
						sessionUtils.getLocale(request),
						RealmService.RESOURCE_BUNDLE, "error.invalidRealmId",
						id));
			}

			String previousName = realm.getName();
			realmService.deleteRealm(realm);

			return new ResourceStatus<Realm>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.realm.deleted",
					previousName));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Realm>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realm/providers", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<RealmProvider> getRealmModules(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			return new ResourceList<RealmProvider>(realmService.getProviders());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "group/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Principal getGroup(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			return realmService
					.getPrincipalById(realm, id, PrincipalType.GROUP);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "user/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public UserUpdate getUserProperties(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			UserUpdate user = new UserUpdate();
			Principal principal = realmService.getPrincipalById(realm, id,
					PrincipalType.USER);
			user.setId(principal.getId());
			user.setName(principal.getPrincipalName());

			return user;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "group", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> createOrUpdateGroup(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody GroupUpdate group) throws UnauthorizedException,
			SessionTimeoutException, AccessDeniedException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = sessionUtils.getCurrentRealm(request);

			List<Principal> principals = new ArrayList<Principal>();
			for (Long user : group.getUsers()) {
				principals.add(realmService.getPrincipalById(realm, user,
						PrincipalType.USER));
			}

			Principal principal;

			if (group.getId() == null) {
				principal = realmService.createGroup(realm, group.getName(),
						principals);
			} else {
				principal = realmService.getPrincipalById(realm, group.getId(),
						PrincipalType.GROUP);
				principal = realmService.updateGroup(realm, principal,
						group.getName(), principals);
			}

			return new ResourceStatus<Principal>(principal, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					group.getId() != null ? "info.group.updated"
							: "info.group.created", principal
							.getPrincipalName()));

		} catch (ResourceCreationException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "group/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> deleteGroup(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			Principal group = realmService.getPrincipalById(realm, id,
					PrincipalType.GROUP);
			String oldName = group.getPrincipalName();
			realmService.deleteGroup(realm, group);

			return new ResourceStatus<Principal>(true,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE, "info.group.deleted",
							oldName));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "user/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> deleteUser(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			Principal user = realmService.getPrincipalById(realm, id,
					PrincipalType.USER);
			String oldName = user.getPrincipalName();
			realmService.deleteUser(realm, user);

			return new ResourceStatus<Principal>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.user.deleted", oldName));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "user", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> createOrUpdateUser(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserUpdate user) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : user.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			Realm realm = sessionUtils.getCurrentRealm(request);

			List<Principal> principals = new ArrayList<Principal>();
			for (Long group : user.getGroups()) {
				principals.add(realmService.getPrincipalById(realm, group,
						PrincipalType.GROUP));
			}

			Principal principal;

			if (user.getId() != null) {
				principal = realmService.getPrincipalById(realm, user.getId(),
						PrincipalType.USER);
				principal = realmService.updateUser(realm, principal,
						user.getName(), properties, principals);
			} else {
				principal = realmService.createUser(realm, user.getName(),
						properties, principals);
			}
			return new ResourceStatus<Principal>(principal, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					user.getId() != null ? "info.group.updated"
							: "info.group.created", principal
							.getPrincipalName()));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} catch (ResourceCreationException e) {
			return new ResourceStatus<Principal>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "credentials", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public CredentialsStatus updateCredentials(HttpServletRequest request,
			HttpServletResponse response, @RequestBody CredentialsUpdate creds)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = sessionUtils.getCurrentRealm(request);

			Principal principal = realmService.getPrincipalById(realm,
					creds.getPrincipalId(), PrincipalType.USER);

			realmService.setPassword(principal, creds.getPassword(),
					creds.isForceChange());

			return new CredentialsStatus(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.credentialsSet",
					principal.getName()));

		} catch (ResourceCreationException e) {
			return new CredentialsStatus(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));
		} finally {
			clearAuthenticatedContext();
		}
	}
}
