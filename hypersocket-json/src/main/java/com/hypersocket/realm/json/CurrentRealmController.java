package com.hypersocket.realm.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.PrincipalNotFoundException;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalColumns;
import com.hypersocket.realm.PrincipalSuspension;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalSuspensionType;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmColumns;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.realm.UserVariableReplacementService;
import com.hypersocket.realm.ou.OrganizationalUnit;
import com.hypersocket.realm.ou.OrganizationalUnitService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;
import com.hypersocket.utils.HypersocketUtils;

@Controller
public class CurrentRealmController extends ResourceController {
	
	@Autowired
	PrincipalSuspensionService suspensionService;

	@Autowired
	UserVariableReplacementService userVariableReplacement;

	@Autowired
	PermissionService permissionService;
	
	@Autowired
	OrganizationalUnitService ouService;
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/groups/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listGroups(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					realmService.allGroups(sessionUtils
							.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/groups/user/{user}", method = RequestMethod.GET, produces = { "application/json" })
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
					realmService.getUserGroups(realmService
							.getPrincipalById(
									sessionUtils.getCurrentRealm(request),
									user, PrincipalType.USER)));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/users/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listUsers(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					realmService.allUsers(sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/users/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableUsers(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		final Realm currentRealm = sessionUtils.getCurrentRealm(request);
		final String module = request.getParameter("filter");

		try {
			BootstrapTableResult<?> r = processDataTablesRequest(request,
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
							return realmService.searchPrincipals(
									currentRealm,
									PrincipalType.USER, module, searchColumn, HypersocketUtils.urlDecode(searchPattern), start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getSearchPrincipalsCount(
									currentRealm,
									PrincipalType.USER, 
									module, 
									searchColumn,
									HypersocketUtils.urlDecode(searchPattern));
						}
					});
			return r;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/groups/filter/{userId}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableRolesFilterByUser(final HttpServletRequest request,
											  HttpServletResponse response,
											@PathVariable("userId") final Long userId) throws AccessDeniedException,
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
							List<?> groups = realmService.allGroups(getCurrentRealm());

							Principal principal = realmService.getPrincipalById(userId);

							final List<Principal> principalGroups = realmService.getUserGroups(principal);

							CollectionUtils.filter(groups, new Predicate() {
								@Override
								public boolean evaluate(Object o) {
									return !principalGroups.contains(o);
								}
							});

							return groups;
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return 0l;
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/groups/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableGroups(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		final Realm currentRealm = sessionUtils.getCurrentRealm(request);
		try {
			return processDataTablesRequest(request,
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
							return realmService.searchPrincipals(
									sessionUtils.getCurrentRealm(request),
									PrincipalType.GROUP,
									currentRealm.getResourceCategory(),
									searchPattern, start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getSearchPrincipalsCount(
									sessionUtils.getCurrentRealm(request),
									PrincipalType.GROUP, 
									currentRealm.getResourceCategory(),
									searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/users/group/{group}", method = RequestMethod.GET, produces = { "application/json" })
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
					realmService.getGroupUsers(realmService
							.getPrincipalById(
									sessionUtils.getCurrentRealm(request),
									group, PrincipalType.GROUP)));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/groups/group/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listGroupGroups(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					realmService.getGroupGroups(realmService
							.getPrincipalById(
									sessionUtils.getCurrentRealm(request),
									group, PrincipalType.GROUP)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserTemplate(
			HttpServletRequest request, @PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					realmService.getUserPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					realmService.getUserPropertyTemplates());
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getGroupTemplate(
			HttpServletRequest request, @PathVariable("module") String module)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					realmService.getGroupPropertyTemplates(module));
		} finally {
			clearAuthenticatedContext();
		}
	}
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/byName/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Principal getGroupByName(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") String name)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			return realmService
					.getPrincipalByName(realm, name, PrincipalType.GROUP);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/byName/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> getUserByName(
			HttpServletRequest request, @PathVariable String name)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Principal principal = realmService.getPrincipalByName(
					sessionUtils.getCurrentRealm(request), name,
					PrincipalType.USER);
			if(principal == null)
				return new ResourceStatus<Principal>(false, "Not found.");
			else
				return new ResourceStatus<Principal>(principal);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserProperties(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

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
	@RequestMapping(value = "currentRealm/group/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getGroupProperties(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Principal principal = realmService.getPrincipalById(
					sessionUtils.getCurrentRealm(request), id,
					PrincipalType.GROUP);
			return new ResourceList<PropertyCategory>(
					realmService.getGroupPropertyTemplates(principal));
		} finally {
			clearAuthenticatedContext();
		}
	}

	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/profile", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserProfile(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					realmService.getUserProfileTemplates(sessionUtils
							.getPrincipal(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/profile", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus setUserProperties(HttpServletRequest request,
			@RequestBody PropertyItem[] items) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, ResourceException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Map<String, String> values = new HashMap<String, String>();
			for (PropertyItem item : items) {
				values.put(item.getId(), item.getValue());
			}

			realmService.updateProfile(sessionUtils.getCurrentRealm(request),
					sessionUtils.getPrincipal(request), values);

			return new RequestStatus(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmServiceImpl.RESOURCE_BUNDLE, "profile.saved"));
		} catch (AccessDeniedException | ResourceException e) {
			return new RequestStatus(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/templateNames", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getUserPropertyNames(
			HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(
				realmService.getUserPropertyNames(sessionUtils.getCurrentRealm(request), null));
		} finally {
			clearAuthenticatedContext();
		}

	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/editableNames", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getEditableNames(
			HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(
				realmService.getEditablePropertyNames(sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}

	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/visibleNames", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getVisibleNames(
			HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(
				realmService.getVisiblePropertyNames(sessionUtils.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}

	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/{id}", method = RequestMethod.GET, produces = { "application/json" })
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
	@RequestMapping(value = "currentRealm/user/{id}", method = RequestMethod.GET, produces = { "application/json" })
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
	@RequestMapping(value = "currentRealm/user/variableNames", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<String> getUserVariableNames(
			HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<String>(
				userVariableReplacement.getVariableNames(getCurrentPrincipal()));

		} finally {
			clearAuthenticatedContext();
		}

	}
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/variables", method = {
			RequestMethod.GET, RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Map<String, String>> getUserVariableValues(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String variables) throws UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			String[] variableNames = StringUtils
					.commaDelimitedListToStringArray(variables);

			return new ResourceStatus<Map<String, String>>(
					realmService.getUserPropertyValues(
							sessionUtils.getPrincipal(request), variableNames));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/allVariables", method = {
			RequestMethod.GET, RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Map<String, String>> getAllUserVariableValues(
			HttpServletRequest request, HttpServletResponse response)
			throws UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Collection<String> names = realmService.getUserVariableNames(
					sessionUtils.getCurrentRealm(request),
					sessionUtils.getPrincipal(request));
			names.add("password");
			return new ResourceStatus<Map<String, String>>(
					realmService.getUserPropertyValues(
							sessionUtils.getPrincipal(request),
								names.toArray(new String[0])));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group", method = RequestMethod.POST, produces = { "application/json" })
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

			List<Principal> userPrincipals = new ArrayList<Principal>();
			for (String user : group.getUsers()) {
				userPrincipals.add(realmService.getPrincipalById(realm, Long.parseLong(ResourceUtils.getNamePairKey(user)),
						PrincipalType.USER));
			}
			
			List<Principal> groupPrincipals = new ArrayList<Principal>();
			for (String user : group.getGroups()) {
				groupPrincipals.add(realmService.getPrincipalById(realm, Long.parseLong(ResourceUtils.getNamePairKey(user)),
						PrincipalType.GROUP));
			}

			Principal principal;
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : group.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}
			
			if (group.getId() == null) {
				principal = realmService.createGroup(realm, group.getName(),
						properties, userPrincipals, groupPrincipals);
			} else {
				principal = realmService.getPrincipalById(realm, group.getId(),
						PrincipalType.GROUP);
				principal = realmService.updateGroup(realm, principal,
						group.getName(), properties, userPrincipals, groupPrincipals);
			}

			return new ResourceStatus<Principal>(principal, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					group.getId() != null ? "info.group.updated"
							: "info.group.created", principal
							.getPrincipalName()));

		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
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

		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
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

		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user", method = RequestMethod.POST, produces = { "application/json" })
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
			for (String group : user.getGroups()) {
				principals.add(realmService.getPrincipalById(realm, 
						Long.parseLong(ResourceUtils.getNamePairKey(group)),
						PrincipalType.GROUP));
			}

			Principal principal;

			if (user.getId() != null) {
				principal = realmService.getPrincipalById(realm, user.getId(),
						PrincipalType.USER);
				principal = realmService.updateUser(realm, principal,
						user.getName(), properties, principals);
			} else {
				boolean sendNotifications = !"true".equals(properties.get("disableNotifications"));
				principal = realmService.createUser(realm, user.getName(),
						properties, principals, user.getPassword(),
						user.isForceChange(), false, sendNotifications);
			}
			return new ResourceStatus<Principal>(principal,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							user.getId() != null ? "info.user.updated"
									: "info.user.created", principal
									.getPrincipalName()));

		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/credentials", method = RequestMethod.POST, produces = { "application/json" })
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
					creds.isForceChange(), !getCurrentPrincipal().equals(principal));

			return new CredentialsStatus(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.credentialsSet",
					principal.getName()));

		} catch(AccessDeniedException ex) { 
			return new CredentialsStatus(false, ex.getMessage());
		} catch (ResourceException e) {
			return new CredentialsStatus(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/changePassword", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus changePassword(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "oldPassword") String oldPassword,
			@RequestParam(value = "newPassword") String newPassword)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Principal principal = sessionUtils.getSession(request)
					.getCurrentPrincipal();

			realmService.changePassword(principal, oldPassword, newPassword);

			return new RequestStatus(true,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmServiceImpl.RESOURCE_BUNDLE,
							"password.change.success"));
		} catch (AccessDeniedException | ResourceException re) {
			return new RequestStatus(false, re.getMessage());

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/suspendUser", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PrincipalSuspension> suspendUser(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody PrincipalSuspensionUpdate principalSuspensionUpdate)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		Realm realm = sessionUtils.getCurrentRealm(request);

		Principal principal = realmService.getPrincipalById(realm,
				principalSuspensionUpdate.getPrincipalId(), PrincipalType.USER);
		try {

			PrincipalSuspension principalSuspension = suspensionService
					.createPrincipalSuspension(principal,
							principalSuspensionUpdate.getStartDate(),
							principalSuspensionUpdate.getDuration(),
							PrincipalSuspensionType.MANUAL);

			return new ResourceStatus<PrincipalSuspension>(principalSuspension,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							"suspendUser.suspendSuccess", principalSuspension
									.getPrincipal().getPrincipalName()));

		} catch (ResourceException e) {
			return new ResourceStatus<PrincipalSuspension>(false,
					e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/resumeUser/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PrincipalSuspension> resumeUser(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		Realm realm = sessionUtils.getCurrentRealm(request);

		Principal principal = realmService.getPrincipalById(realm, id,
				PrincipalType.USER);
		try {
			PrincipalSuspension principalSuspension = suspensionService
					.deletePrincipalSuspension(principal,
							PrincipalSuspensionType.MANUAL);

			return new ResourceStatus<PrincipalSuspension>(principalSuspension,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							"suspendUser.resumeSuccess", principal.getPrincipalName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/user/{id}/roles", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Role> getPrincipalRoles(HttpServletRequest request,
										   HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, PrincipalNotFoundException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = sessionUtils.getCurrentRealm(request);

			Principal principal = realmService.getPrincipalById(realm, id,
					PrincipalType.USER);

			if(principal == null) {
				throw new PrincipalNotFoundException(String.format("Principal with id %d not found.", id));
			}

			Set<Role> roles = permissionService.getPrincipalNonPersonalRoles(principal);
			return new ResourceList<>(roles);
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/bulk/groups", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus deleteGroupResources(HttpServletRequest request,
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
			
			List<Principal> groupResources = realmService.getGroupsByIds(ids);

			if(groupResources == null || groupResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				realmService.deleteGroups(getCurrentRealm(), groupResources);
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
	
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/bulk/users", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus deleteUserResources(HttpServletRequest request,
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
			
			List<Principal> userResources = realmService.getUsersByIds(ids);

			if(userResources == null || userResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				realmService.deleteUsers(getCurrentRealm(), userResources);
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
	
	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/{groupId}/user/{userId}", method = RequestMethod.PATCH,
			produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> addUserToGroup(HttpServletRequest request,
												   HttpServletResponse response, 
											 @PathVariable("groupId") Long groupId,
											  @PathVariable("userId") Long userId)
			throws UnauthorizedException, AccessDeniedException,
			SessionTimeoutException, PrincipalNotFoundException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Principal group = realmService.getPrincipalById(groupId);
			Principal principal = realmService.getPrincipalById(userId);

			if(principal == null) {
				throw new PrincipalNotFoundException(String.format("Principal not found for id %d.", userId));
			}

			realmService.assignUserToGroup(principal, group);
			
			return new ResourceStatus<>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
					"group.add.to.user", principal.getPrincipalName(), group.getPrincipalName()));
			
		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<>(false, e.getMessage());
		}finally {
			clearAuthenticatedContext();
		}
	}


	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/group/{groupId}/user/{userId}", method = RequestMethod.DELETE,
			produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> deleteRoleFromUser(HttpServletRequest request,
											  HttpServletResponse response,  
											  @PathVariable("groupId") Long groupId,
											  @PathVariable("userId") Long userId)
			throws UnauthorizedException, AccessDeniedException,
			SessionTimeoutException, PrincipalNotFoundException, ResourceException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

				Principal group = realmService.getPrincipalById(groupId);
				Principal principal = realmService.getPrincipalById(userId);

				if(principal == null) {
					throw new PrincipalNotFoundException(String.format("Principal not found for id %d.", userId));
				}

				realmService.unassignUserFromGroup(principal, group);

				return new ResourceStatus<>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE,
						"group.remove.from.user", principal.getPrincipalName(), group.getPrincipalName()));

		} catch (AccessDeniedException | ResourceException e) {
			return new ResourceStatus<>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "currentRealm/organizationalUnits", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<OrganizationalUnit> listOUs(HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<OrganizationalUnit>(ouService.getOrganizationalUnits(getCurrentRealm()));
		} finally {
			clearAuthenticatedContext();
		}
	}
}
