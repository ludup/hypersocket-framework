/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.ResourceStatusConfirmation;
import com.hypersocket.migration.exception.MigrationProcessRealmAlreadyExistsThrowable;
import com.hypersocket.migration.exception.MigrationProcessRealmNotFoundException;
import com.hypersocket.migration.execution.MigrationExecutor;
import com.hypersocket.migration.execution.MigrationExecutorTracker;
import com.hypersocket.migration.info.MigrationHelperClassesInfoProvider;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.PrincipalColumns;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmColumns;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.realm.UserVariableReplacementService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceConfirmationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceExportException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class RealmController extends ResourceController {

	static Logger log = LoggerFactory.getLogger(RealmController.class);

	@Autowired
	UserVariableReplacementService userVariableReplacement;

	@Autowired
	MigrationExecutor migrationExecutor;

	@Autowired
	MigrationHelperClassesInfoProvider migrationHelperClassesInfoProvider;

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Realm getRealm(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

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
	public ResourceStatus<Realm> setDefaultRealm(HttpServletRequest request, HttpServletResponse response,
												 @PathVariable("id") Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.setDefaultRealm(realmService.getRealmById(id));
			return new ResourceStatus<Realm>(realm, I18N.getResource(sessionUtils.getLocale(request),
					RealmServiceImpl.RESOURCE_BUNDLE, "realm.madeDefault", realm.getName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Realm> listRealms(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

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
	public BootstrapTableResult<?> tableRealms(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

				@Override
				public Column getColumn(String col) {
					return RealmColumns.valueOf(col.toUpperCase());
				}

				@Override
				public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
									   ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
					return realmService.getRealms(searchPattern, start, length, sorting);
				}

				@Override
				public Long getTotalCount(String searchColumn, String searchPattern)
						throws UnauthorizedException, AccessDeniedException {
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
	public ResourceList<PropertyCategory> getRealmTemplate(HttpServletRequest request,
														   @PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(realmService.getRealmPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/properties/{id}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRealmPropertiesJson(HttpServletRequest request,
																 @PathVariable("id") Long module)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.getRealmById(module);
			return new ResourceList<PropertyCategory>(realmService.getRealmPropertyTemplates(realm));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm/userVariables/{id}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getUserVariableNames(HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(userVariableReplacement.getVariableNames(realmService.getRealmById(id)));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/realm", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Realm> createOrUpdateRealm(HttpServletRequest request, HttpServletResponse response,
													 @RequestBody RealmUpdate realm)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : realm.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			Realm newRealm;

			if (realm.getId() != null) {
				newRealm = realmService.updateRealm(realmService.getRealmById(realm.getId()), realm.getName(),
						realm.getType(), properties);
			} else {
				newRealm = realmService.createPrimaryRealm(realm.getName(), realm.getType(), properties);
			}

			return new ResourceStatus<Realm>(newRealm,
					I18N.getResource(sessionUtils.getLocale(request), RealmService.RESOURCE_BUNDLE,
							realm.getId() != null ? "info.realm.updated" : "info.realm.created", realm.getName()));

		} catch (ResourceConfirmationException e) {
			return new ResourceStatusConfirmation<Realm>(e.getMessage(), e.getOptions(), e.getArgs());
		} catch (ResourceException e) {
			return new ResourceStatus<Realm>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/reset/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Realm> resetRealm(HttpServletRequest request, HttpServletResponse response,
											 @PathVariable("id") Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				return new ResourceStatus<Realm>(false, I18N.getResource(sessionUtils.getLocale(request),
						RealmService.RESOURCE_BUNDLE, "error.invalidRealmId", id));
			}

			realmService.resetRealm(realm);

			return new ResourceStatus<Realm>(true, I18N.getResource(sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.realm.reset"));

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
	public ResourceStatus<Realm> deleteRealm(HttpServletRequest request, HttpServletResponse response,
											 @PathVariable("id") Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				return new ResourceStatus<Realm>(false, I18N.getResource(sessionUtils.getLocale(request),
						RealmService.RESOURCE_BUNDLE, "error.invalidRealmId", id));
			}

			String previousName = realm.getName();
			realmService.deleteRealm(realm);

			return new ResourceStatus<Realm>(true, I18N.getResource(sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.realm.deleted", previousName));

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
	public ResourceList<RealmProvider> getRealmModules(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));
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
	public BootstrapTableResult<?> tableUsers(final HttpServletRequest request, HttpServletResponse response,
											  @PathVariable Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {

			final Realm realm = realmService.getRealmById(id);

			BootstrapTableResult<?> r = processDataTablesRequest(request, new BootstrapTablePageProcessor() {

				@Override
				public Column getColumn(String col) {
					return PrincipalColumns.valueOf(col.toUpperCase());
				}

				@Override
				public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
									   ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
					return realmService.searchPrincipals(realm, PrincipalType.USER, realm.getResourceCategory(),
							searchPattern, start, length, sorting);
				}

				@Override
				public Long getTotalCount(String searchColumn, String searchPattern)
						throws UnauthorizedException, AccessDeniedException {
					return realmService.getSearchPrincipalsCount(realm, PrincipalType.USER, realm.getResourceCategory(),
							searchPattern);
				}
			});
			return r;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "realms/export", method = RequestMethod.POST, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public void exportAll(HttpServletRequest request,
						  HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException,
			ResourceNotFoundException, ResourceExportException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		ZipOutputStream zos = null;
		try {
			Boolean all = Boolean.parseBoolean(request.getParameter("all"));
			String list = request.getParameter("list");
			Long realmId = Long.parseLong(request.getParameter("resource"));

			log.info("Starting realm export for realm id {} with all {} and list {}", realmId, all, list);

			String[] entities = list.split(",");

			response.reset();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ "realmResource.zip\"");
			zos = new
					ZipOutputStream(response.getOutputStream());
			realmService.exportResources(zos, realmId, all, entities);

		} catch (IOException e) {
			log.error("Problem in exporting realm resource.", e);
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			if(zos != null) {
				try {
					zos.flush();
					zos.close();
				}catch (IOException e) {
					//ignore
				}
			}
			clearAuthenticatedContext();
		}

	}


	@AuthenticationRequired
	@RequestMapping(value = "realms/import", method = { RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<String> importRealm(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile zipFile,
			@RequestParam(required = false) boolean mergeData)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}

		MigrationExecutorTracker migrationExecutorTracker = null;
		try {

			migrationExecutorTracker = migrationExecutor.startRealmImport(zipFile.getInputStream(), mergeData);
			String msgKey = "realm.import.success";

			if(migrationExecutorTracker.getFailure() != 0 || migrationExecutorTracker.getCustomOperationFailure() != 0) {
				msgKey = "realm.import.partial.success";
				migrationExecutorTracker.logErrorNodes();
			}

			return new ResourceStatus<String>(true,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							msgKey));

		} catch (MigrationProcessRealmAlreadyExistsThrowable e) {
			log.error("Problem in importing realm resource.", e);
			return new ResourceStatus<String>(false, I18N.getResource(sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					"realm.import.no.merge.realm.exists.failure"));
		} catch(MigrationProcessRealmNotFoundException e) {
			log.error("Problem in importing realm resource.", e);
			return new ResourceStatus<String>(false, I18N.getResource(sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					"realm.import.no.realm.failure"));
		} catch (Exception e) {
			if(migrationExecutorTracker != null) {
				migrationExecutorTracker.logErrorNodes();
			}
			log.error("Problem in importing realm resource.", e);
			return new ResourceStatus<String>(false, I18N.getResource(sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					"realm.import.failure"));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "exportables/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getExportables(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceList<String>(
					migrationHelperClassesInfoProvider.getAllExportableClasses());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "realms/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus deleteResources(HttpServletRequest request,
												HttpServletResponse response,
												@RequestBody Long[] ids)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			
			if(ids == null) {
				ids = new Long[0];
			}
			
			List<Realm> realmResources = realmService.getRealmsByIds(ids);

			if(realmResources == null || realmResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				realmService.deleteRealms(realmResources);
				return new RequestStatus(true,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.success"));
			}
			
		} catch (Exception e) {
			return new RequestStatus(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
}
