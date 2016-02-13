package com.hypersocket.permissions.json;

import java.util.ArrayList;
import java.util.List;

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

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmColumns;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class RoleController extends ResourceController {

	@Autowired
	PermissionService permissionService;

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
					permissionService.getRoleTemplates());
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
		} catch (ResourceChangeException e) {
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
	@RequestMapping(value = "roles/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableRoles(final HttpServletRequest request,
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
							return permissionService.getRoles(searchPattern,
									start, length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return permissionService
									.getRoleCount(searchPattern);
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

			List<Principal> principals = new ArrayList<Principal>();
			for (Long user : role.getUsers()) {
				principals.add(realmService.getPrincipalById(realm, user,
						PrincipalType.USER));
			}

			for (Long group : role.getGroups()) {
				principals.add(realmService.getPrincipalById(realm, group,
						PrincipalType.GROUP));
			}

			List<Permission> permissions = new ArrayList<Permission>();
			for (Long perm : role.getPermissions()) {
				permissions.add(permissionService.getPermissionById(perm));
			}

			if (role.getId() == null) {
				newRole = permissionService.createRole(role.getName(), realm,
						principals, permissions);
			} else {
				newRole = permissionService.updateRole(
						permissionService.getRoleById(role.getId(), realm),
						role.getName(), principals, permissions);
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
}
