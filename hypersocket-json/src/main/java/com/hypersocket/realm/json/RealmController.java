/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

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
import com.hypersocket.realm.PrincipalColumns;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmColumns;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class RealmController extends ResourceController {

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/{id}", method = RequestMethod.GET, produces = { "application/json" })
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
	@RequestMapping(value = "realms/default/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Realm> setDefaultRealm(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.setDefaultRealm(realmService
					.getRealmById(id));
			return new ResourceStatus<Realm>(realm, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmServiceImpl.RESOURCE_BUNDLE, "realm.madeDefault",
					realm.getName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/list", method = RequestMethod.GET, produces = { "application/json" })
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
	@RequestMapping(value = "realms/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableRealms(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return RealmColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getRealms(searchPattern, start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getRealmCount(searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRealmTemplate(
			HttpServletRequest request, @PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					realmService.getRealmPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRealmPropertiesJson(
			HttpServletRequest request, @PathVariable("id") Long module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
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
	@RequestMapping(value = "realms/realm/userVariables/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getUserVariableNames(
			HttpServletRequest request,
			@PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(
				realmService.getUserVariableNames(realmService.getRealmById(id), null));

		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "realms/realm", method = RequestMethod.POST, produces = { "application/json" })
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
						realm.getType(), null, properties);
			}
			return new ResourceStatus<Realm>(newRealm, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					realm.getId() != null ? "info.realm.updated"
							: "info.realm.created", realm.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Realm>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
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
			return new ResourceStatus<Realm>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/providers", method = RequestMethod.GET, produces = { "application/json" })
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
	@RequestMapping(value = "realms/users/table/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableUsers(final HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			
			final Realm realm  = realmService.getRealmById(id);
			
			BootstrapTableResult r = processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return PrincipalColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getPrincipals(
									realm,
									PrincipalType.USER, searchPattern, start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getPrincipalCount(
									realm,
									PrincipalType.USER, searchPattern);
						}
					});
			return r;
		} finally {
			clearAuthenticatedContext();
		}
	}
}
