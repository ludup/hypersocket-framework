package com.hypersocket.realm.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.account.linking.AccountLinkingService;
import com.hypersocket.account.linking.AccountLinkingServiceImpl;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalColumns;
import com.hypersocket.realm.PrincipalSuspension;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserVariableReplacement;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class OwnerRealmController extends ResourceController {

	@Autowired
	PrincipalSuspensionService suspensionService;

	@Autowired
	UserVariableReplacement userVariableReplacement;
	
	@Autowired
	AccountLinkingService linkingService; 
	
	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/groups/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listOwnerGroups(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm ownerRealm = realmService.getRealmById(id);
			return new ResourceList<Principal>(
					realmService.allGroups(ownerRealm));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/groups/user/{user}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listOwnerGroups(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @PathVariable Long user)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm ownerRealm = realmService.getRealmById(id);
			return new ResourceList<Principal>(
					realmService.getUserGroups(realmService
							.getPrincipalById(
									ownerRealm,
									user, PrincipalType.USER)));
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/users/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listOwnerUsers(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm ownerRealm = realmService.getRealmById(id);
			return new ResourceList<Principal>(
					realmService.allUsers(ownerRealm));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/users/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableUsers(final HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			final Realm ownerRealm = realmService.getRealmById(id);
			
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
									ownerRealm,
									PrincipalType.USER, searchPattern, start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getSearchPrincipalsCount(
									ownerRealm,
									PrincipalType.USER, searchPattern);
						}
					});
			return r;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/groups/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableGroups(final HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			final Realm ownerRealm = realmService.getRealmById(id);
			
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
									ownerRealm,
									PrincipalType.GROUP, searchPattern, start,
									length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return realmService.getSearchPrincipalsCount(
									ownerRealm,
									PrincipalType.GROUP, searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/users/group/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listUsers(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			
			Realm ownerRealm = realmService.getRealmById(id);
			
			return new ResourceList<Principal>(
					realmService.getGroupUsers(realmService
							.getPrincipalById(
									ownerRealm,
									group, PrincipalType.GROUP)));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/groups/group/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listGroupGroups(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			
			Realm ownerRealm = realmService.getRealmById(id);
			
			return new ResourceList<Principal>(
					realmService.getGroupGroups(realmService
							.getPrincipalById(
									ownerRealm,
									group, PrincipalType.GROUP)));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/user/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserTemplate(
			HttpServletRequest request, 
			@PathVariable Long id,
			@PathVariable("module") String module)
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

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/template", method = RequestMethod.GET, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceList<PropertyCategory> getUserTemplate(
//			HttpServletRequest request) throws AccessDeniedException,
//			UnauthorizedException, SessionTimeoutException {
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		try {
//			return new ResourceList<PropertyCategory>(
//					realmService.getUserPropertyTemplates());
//		} finally {
//			clearAuthenticatedContext();
//		}
//
//	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/group/template/{module}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getGroupTemplate(
			HttpServletRequest request, 
			@PathVariable Long id,
			@PathVariable("module") String module)
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
	@RequestMapping(value = "ownerRealm/{id}/user/properties/{user}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getUserProperties(
			HttpServletRequest request, @PathVariable Long id, @PathVariable Long user)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm ownerRealm = realmService.getRealmById(id);
			
			Principal principal = realmService.getPrincipalById(
					ownerRealm, user,
					PrincipalType.USER);
			return new ResourceList<PropertyCategory>(
					realmService.getUserPropertyTemplates(principal));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/group/properties/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getGroupProperties(
			HttpServletRequest request, @PathVariable Long id, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			
			Realm ownerRealm = realmService.getRealmById(id);
			
			Principal principal = realmService.getPrincipalById(
					ownerRealm, group,
					PrincipalType.GROUP);
			return new ResourceList<PropertyCategory>(
					realmService.getGroupPropertyTemplates(principal));
		} finally {
			clearAuthenticatedContext();
		}
	}

	
//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/profile", method = RequestMethod.GET, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceList<PropertyCategory> getUserProfile(
//			HttpServletRequest request) throws AccessDeniedException,
//			UnauthorizedException, SessionTimeoutException {
//
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		try {
//			return new ResourceList<PropertyCategory>(
//					realmService.getUserProfileTemplates(sessionUtils
//							.getPrincipal(request)));
//		} finally {
//			clearAuthenticatedContext();
//		}
//	}

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/profile", method = RequestMethod.POST, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public RequestStatus setUserProperties(HttpServletRequest request,
//			@RequestBody PropertyItem[] items) throws AccessDeniedException,
//			UnauthorizedException, SessionTimeoutException {
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		try {
//			Map<String, String> values = new HashMap<String, String>();
//			for (PropertyItem item : items) {
//				values.put(item.getId(), item.getValue());
//			}
//
//			realmService.updateProfile(sessionUtils.getCurrentRealm(request),
//					sessionUtils.getPrincipal(request), values);
//
//			return new RequestStatus(true, I18N.getResource(
//					sessionUtils.getLocale(request),
//					RealmServiceImpl.RESOURCE_BUNDLE, "profile.saved"));
//		} catch (ResourceChangeException e) {
//			return new RequestStatus(false, e.getMessage());
//		} finally {
//			clearAuthenticatedContext();
//		}
//	}

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/templateNames", method = RequestMethod.GET, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceList<String> getUserPropertyNames(
//			HttpServletRequest request)
//			throws AccessDeniedException, UnauthorizedException,
//			SessionTimeoutException {
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		try {
//			return new ResourceList<String>(
//				realmService.getUserPropertyNames(sessionUtils.getCurrentRealm(request), null));
//		} finally {
//			clearAuthenticatedContext();
//		}
//
//	}
	
	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/group/{group}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Principal getGroup(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id,
			@PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.getRealmById(id);
			return realmService
					.getPrincipalById(realm, group, PrincipalType.GROUP);
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/user/{user}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public UserUpdate getUserProperties(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id, @PathVariable Long user)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Realm realm = realmService.getRealmById(id);
			UserUpdate userUpdate = new UserUpdate();
			Principal principal = realmService.getPrincipalById(realm, user,
					PrincipalType.USER);
			userUpdate.setId(principal.getId());
			userUpdate.setName(principal.getPrincipalName());

			return userUpdate;
		} finally {
			clearAuthenticatedContext();
		}
	}

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/variableNames", method = RequestMethod.GET, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceList<String> getUserVariableNames(
//			HttpServletRequest request)
//			throws AccessDeniedException, UnauthorizedException,
//			SessionTimeoutException {
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//
//		try {
//			return new ResourceList<String>(
//				userVariableReplacement.getVariableNames(getCurrentPrincipal()));
//
//		} finally {
//			clearAuthenticatedContext();
//		}
//
//	}
	
//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/variables", method = {
//			RequestMethod.GET, RequestMethod.POST }, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceStatus<Map<String, String>> getUserVariableValues(
//			HttpServletRequest request, HttpServletResponse response,
//			@RequestParam String variables) throws UnauthorizedException,
//			SessionTimeoutException {
//
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//		try {
//			String[] variableNames = StringUtils
//					.commaDelimitedListToStringArray(variables);
//
//			return new ResourceStatus<Map<String, String>>(
//					realmService.getUserPropertyValues(
//							sessionUtils.getPrincipal(request), variableNames));
//		} finally {
//			clearAuthenticatedContext();
//		}
//	}

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/allVariables", method = {
//			RequestMethod.GET, RequestMethod.POST }, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public ResourceStatus<Map<String, String>> getAllUserVariableValues(
//			HttpServletRequest request, HttpServletResponse response)
//			throws UnauthorizedException, SessionTimeoutException {
//
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//		try {
//			Collection<String> names = realmService.getUserVariableNames(
//					sessionUtils.getCurrentRealm(request),
//					sessionUtils.getPrincipal(request));
//			names.add("password");
//			return new ResourceStatus<Map<String, String>>(
//					realmService.getUserPropertyValues(
//							sessionUtils.getPrincipal(request),
//								names.toArray(new String[0])));
//		} finally {
//			clearAuthenticatedContext();
//		}
//	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/group/", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> createOrUpdateGroup(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@RequestBody GroupUpdate group) throws UnauthorizedException,
			SessionTimeoutException, AccessDeniedException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);

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

		} catch (ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/group/{group}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> deleteGroup(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id, @PathVariable Long group)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Realm realm = realmService.getRealmById(id);
			Principal groupUpdate = realmService.getPrincipalById(realm, group,
					PrincipalType.GROUP);
			String oldName = groupUpdate.getPrincipalName();
			realmService.deleteGroup(realm, groupUpdate);

			return new ResourceStatus<Principal>(true,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE, "info.group.deleted",
							oldName));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/user/{user}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> deleteUser(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id, @PathVariable Long user)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Realm realm = realmService.getRealmById(id);
			Principal userUpdate = realmService.getPrincipalById(realm, user,
					PrincipalType.USER);
			String oldName = userUpdate.getPrincipalName();
			realmService.deleteUser(realm, userUpdate);

			return new ResourceStatus<Principal>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					RealmService.RESOURCE_BUNDLE, "info.user.deleted", oldName));

		} catch (ResourceChangeException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/user", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> createOrUpdateUser(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@RequestBody UserUpdate user) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : user.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			Realm realm = realmService.getRealmById(id);

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
				principal = realmService.createUser(realm, user.getName(),
						properties, principals, user.getPassword(),
						user.isForceChange(), false);
			}
			return new ResourceStatus<Principal>(principal,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							user.getId() != null ? "info.user.updated"
									: "info.user.created", principal
									.getPrincipalName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/linkAccount/{secondary}/{primary}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> linkAccount(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@PathVariable Long secondary,
			@PathVariable Long primary) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);
			
			Principal primaryPrincipal = realmService.getPrincipalById(getCurrentRealm(), primary, PrincipalType.USER);
			Principal secondaryPrincipal = realmService.getPrincipalById(realm, secondary, PrincipalType.USER);
			
			linkingService.linkAccounts(primaryPrincipal, secondaryPrincipal);
			
			return new ResourceStatus<Principal>(secondaryPrincipal,
					I18N.getResource(sessionUtils.getLocale(request),
							AccountLinkingServiceImpl.RESOURCE_BUNDLE,
							"info.linkedAccount", secondaryPrincipal.getPrincipalName(),
							primaryPrincipal.getPrincipalName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/unlinkAccount/{secondary}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Principal> unlinkAccount(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@PathVariable Long secondary) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);
			
			Principal secondaryPrincipal = realmService.getPrincipalById(realm, secondary, PrincipalType.USER);
			Principal primaryPrincipal = secondaryPrincipal.getParentPrincipal();
			
			linkingService.unlinkAccounts(primaryPrincipal, secondaryPrincipal);
			
			return new ResourceStatus<Principal>(secondaryPrincipal,
					I18N.getResource(sessionUtils.getLocale(request),
							AccountLinkingServiceImpl.RESOURCE_BUNDLE,
							"info.unlinkedAccount", secondaryPrincipal.getPrincipalName(),
							primaryPrincipal.getPrincipalName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/user/credentials", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public CredentialsStatus updateCredentials(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable Long id,
			@RequestBody CredentialsUpdate creds)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Realm realm = realmService.getRealmById(id);

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

//	@AuthenticationRequired
//	@RequestMapping(value = "ownerRealm/user/changePassword", method = RequestMethod.POST, produces = { "application/json" })
//	@ResponseBody
//	@ResponseStatus(value = HttpStatus.OK)
//	public RequestStatus changePassword(HttpServletRequest request,
//			HttpServletResponse response,
//			@PathVariable Long id,
//			@RequestParam(value = "oldPassword") String oldPassword,
//			@RequestParam(value = "newPassword") String newPassword)
//			throws AccessDeniedException, UnauthorizedException,
//			SessionTimeoutException {
//		setupAuthenticatedContext(sessionUtils.getSession(request),
//				sessionUtils.getLocale(request));
//		try {
//			Principal principal = sessionUtils.getSession(request)
//					.getCurrentPrincipal();
//
//			realmService.changePassword(principal, oldPassword, newPassword);
//
//			return new RequestStatus(true,
//					I18N.getResource(sessionUtils.getLocale(request),
//							RealmServiceImpl.RESOURCE_BUNDLE,
//							"password.change.success"));
//		} catch (ResourceException re) {
//			return new RequestStatus(false, re.getMessage());
//
//		} finally {
//			clearAuthenticatedContext();
//		}
//	}

	@AuthenticationRequired
	@RequestMapping(value = "ownerRealm/{id}/suspendUser", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PrincipalSuspension> suspendUser(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@RequestBody PrincipalSuspensionUpdate principalSuspensionUpdate)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		Realm realm = realmService.getRealmById(id);

		Principal principal = realmService.getPrincipalById(realm,
				principalSuspensionUpdate.getPrincipalId(), PrincipalType.USER);
		try {

			PrincipalSuspension principalSuspension = suspensionService
					.createPrincipalSuspension(principal,
							principalSuspensionUpdate.getStartDate(),
							principalSuspensionUpdate.getDuration());

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
	@RequestMapping(value = "ownerRealm/{id}/resumeUser/{user}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PrincipalSuspension> resumeUser(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@PathVariable("id") Long user) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		
		Realm realm = realmService.getRealmById(id);

		Principal principal = realmService.getPrincipalById(realm, user,
				PrincipalType.USER);
		try {
			PrincipalSuspension principalSuspension = suspensionService
					.deletePrincipalSuspension(principal);

			return new ResourceStatus<PrincipalSuspension>(principalSuspension,
					I18N.getResource(sessionUtils.getLocale(request),
							RealmService.RESOURCE_BUNDLE,
							"suspendUser.resumeSuccess", principalSuspension
									.getPrincipal().getPrincipalName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

}
