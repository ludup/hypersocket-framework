package com.hypersocket.permissions.json;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.RoleColumns;
import com.hypersocket.permissions.RoleType;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class RoleController extends ResourceController {
	
	@Autowired
	private PermissionService permissionService;

	@AuthenticationRequired
	@RequestMapping(value = "roles/role/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Role getRole(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return permissionService.getRoleById(id,
					sessionUtils.getCurrentRealm(request));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/byName/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Role getRole(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("name") String name)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return permissionService.getRole(name,
					sessionUtils.getCurrentRealm(request));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRoleTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					permissionService.getRoleTemplate());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getRoleProperties(
			HttpServletRequest request, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					permissionService.getRoleProperties(permissionService.getRoleById(id, getCurrentRealm())));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/role/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Role> deleteRole(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceChangeException, ResourceNotFoundException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {

			Role role = permissionService.getRoleById(id,
					sessionUtils.getCurrentRealm(request));

			permissionService.deleteRole(role);

			return new ResourceStatus<Role>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					PermissionService.RESOURCE_BUNDLE, "info.role.deleted",
					role.getName()));
		} catch (ResourceException e) {
			return new ResourceStatus<Role>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Role> listRoles(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Role>(
					permissionService.allRoles(sessionUtils
							.getCurrentRealm(request)));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/personal", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Role> listPersonalRoles(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Role>(
					permissionService.getPrincipalRoles(getCurrentPrincipal()));
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/permissions/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Permission> listRolePermissions(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Permission>(
					permissionService.getRoleById(id, getCurrentRealm()).getPermissions());
		} catch (ResourceNotFoundException e) {
			return new ResourceList<Permission>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/realms/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Realm> listRoleRealms(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Realm>(
					permissionService.getRoleById(id, getCurrentRealm()).getPermissionRealms());
		} catch (ResourceNotFoundException e) {
			return new ResourceList<Realm>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/principals/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<Principal> listRoleUsers(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<Principal>(
					permissionService.getRoleById(id, getCurrentRealm()).getPrincipals());
		} catch (ResourceNotFoundException e) {
			return new ResourceList<Principal>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableRoles(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return RoleColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService.getRoles(searchPattern, searchColumn,
									start, length, sorting, false, RoleType.BUILTIN, RoleType.CUSTOM);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService
									.getRoleCount(searchPattern, searchColumn, false, RoleType.BUILTIN, RoleType.CUSTOM);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/delegatable", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> delegatableRoles(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return RoleColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService.getRoles(searchPattern, searchColumn,
									start, length, sorting, true, RoleType.BUILTIN, RoleType.CUSTOM);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService
									.getRoleCount(searchPattern, searchColumn, true, RoleType.BUILTIN, RoleType.CUSTOM);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/tableWithUsers", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableRolesWithUsers(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return RoleColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService.getRoles(searchPattern, searchColumn,
									start, length, sorting, false, RoleType.BUILTIN, RoleType.CUSTOM, RoleType.USER);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService
									.getRoleCount(searchPattern, searchColumn, false, RoleType.BUILTIN, RoleType.CUSTOM, RoleType.USER);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/tableAllRoles", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult<?> tableAllRoles(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return RoleColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService.getRoles(searchPattern, searchColumn,
									start, length, sorting, false, RoleType.BUILTIN, RoleType.CUSTOM, RoleType.USER, RoleType.GROUP);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService
									.getRoleCount(searchPattern, searchColumn, false, RoleType.BUILTIN, RoleType.CUSTOM, RoleType.USER, RoleType.GROUP);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/role", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Role> createOrUpdateRole(HttpServletRequest request,
			HttpServletResponse response, @RequestBody RoleUpdate role)
			throws UnauthorizedException, AccessDeniedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Role newRole;
			Realm realm = sessionUtils.getCurrentRealm(request);

			List<Realm> realms = new ArrayList<Realm>();
			for (Long id : role.getRealms()) {
				realms.add(realmService.getRealmById(id));
			}
			
			List<Principal> principals = new ArrayList<Principal>();
			for (String user : role.getUsers()) {
				principals.add(realmService.getPrincipalById(realm, 
						Long.parseLong(ResourceUtils.getNamePairKey(user)),
						PrincipalType.USER));
			}

			for (String group : role.getGroups()) {
				principals.add(realmService.getPrincipalById(realm, 
						Long.parseLong(ResourceUtils.getNamePairKey(group)),
						PrincipalType.GROUP));
			}

			List<Permission> permissions = new ArrayList<Permission>();
			for (Long perm : role.getPermissions()) {
				permissions.add(permissionService.getPermissionById(perm));
			}
			
			Map<String, String> values = new HashMap<String, String>();
			for (PropertyItem item : role.getProperties()) {
				values.put(item.getId(), item.getValue());
			}

			if (role.getId() == null) {
				newRole = permissionService.createRole(role.getName(), realm,
						principals, permissions, realms, values, RoleType.CUSTOM, role.isAllUsers(), role.isAllPerms(), role.isAllRealms());
			} else {
				newRole = permissionService.updateRole(
						permissionService.getRoleById(role.getId(), realm),
						role.getName(), principals, permissions, realms, values, role.isAllUsers(), role.isAllPerms(), role.isAllRealms());
			}

			return new ResourceStatus<Role>(newRole, I18N.getResource(
					sessionUtils.getLocale(request),
					PermissionService.RESOURCE_BUNDLE,
					role.getId() != null ? "info.role.updated"
							: "info.role.created", newRole.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Role>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/{roleId}/user/{userId}", method = RequestMethod.PATCH,
			produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> addRoleToUser(HttpServletRequest request,
												   HttpServletResponse response, @PathVariable("roleId") Long roleId,
											  @PathVariable("userId") Long userId)
			throws UnauthorizedException, AccessDeniedException,
			SessionTimeoutException, PrincipalNotFoundException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Realm realm = sessionUtils.getCurrentRealm(request);
			Role role = permissionService.getRoleById(roleId, realm);
			Principal principal = realmService.getPrincipalById(userId);

			if(principal == null) {
				throw new PrincipalNotFoundException(String.format("Principal not found for id %d.", userId));
			}

			permissionService.assignRole(role, principal);

			return new ResourceStatus<>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					PermissionService.RESOURCE_BUNDLE,
					"role.add.to.user"));
		} catch (ResourceNotFoundException e) {
			return new ResourceStatus<>(false, e.getMessage());
		}finally {
			clearAuthenticatedContext();
		}
	}


	@AuthenticationRequired
	@RequestMapping(value = "roles/{roleId}/user/{userId}", method = RequestMethod.DELETE,
			produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> deleteRoleFromUser(HttpServletRequest request,
											  HttpServletResponse response,  @PathVariable("roleId") Long roleId,
													  @PathVariable("userId") Long userId)
			throws UnauthorizedException, AccessDeniedException,
			SessionTimeoutException, PrincipalNotFoundException, ResourceException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
				Realm realm = sessionUtils.getCurrentRealm(request);
				Role role = permissionService.getRoleById(roleId, realm);
				Principal principal = realmService.getPrincipalById(userId);

				if(principal == null) {
					throw new PrincipalNotFoundException(String.format("Principal not found for id %d.", userId));
				}

				permissionService.unassignRole(role, principal);

				return new ResourceStatus<>(true, I18N.getResource(
					sessionUtils.getLocale(request),
						PermissionService.RESOURCE_BUNDLE,
						"role.remove.from.user"));

		} catch (ResourceException e) {
			return new ResourceStatus<>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "roles/filter/user/{userId}", method = RequestMethod.GET, produces = { "application/json" })
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
							return RoleColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
											   int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							List<?> roles = permissionService.getNoPersonalNoAllUsersRoles(searchPattern,
									start, length, sorting);

							Principal principal = realmService.getPrincipalById(userId);

							final Set<Role> principalRoles = permissionService.getPrincipalNonPersonalNonAllUserRoles(principal);

							CollectionUtils.filter(roles, new Predicate() {
								@Override
								public boolean evaluate(Object o) {
									return !principalRoles.contains(o);
								}
							});

							return roles;
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {

							//no op
							return 0l;
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "roles/bulk", method = RequestMethod.DELETE, produces = { "application/json" })
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
			
			List<Role> realmResources = permissionService.getResourcesByIds(ids);

			if(realmResources == null || realmResources.isEmpty()) {
				return new RequestStatus(false,
						I18N.getResource(sessionUtils.getLocale(request),
								I18NServiceImpl.USER_INTERFACE_BUNDLE,
								"bulk.delete.empty"));
			}else {
				permissionService.deleteResources(realmResources);
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
