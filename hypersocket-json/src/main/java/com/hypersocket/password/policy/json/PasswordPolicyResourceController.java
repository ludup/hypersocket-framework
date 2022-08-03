package com.hypersocket.password.policy.json;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.password.policy.PasswordAnalyserService;
import com.hypersocket.password.policy.PasswordPolicyException;
import com.hypersocket.password.policy.PasswordPolicyResource;
import com.hypersocket.password.policy.PasswordPolicyResourceColumns;
import com.hypersocket.password.policy.PasswordPolicyResourceService;
import com.hypersocket.password.policy.PasswordPolicyResourceServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.RoleUtils;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AssignableResourceUpdate;
import com.hypersocket.resource.ResourceColumns;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;
import com.hypersocket.utils.HypersocketUtils;

@Controller
public class PasswordPolicyResourceController extends ResourceController {

	@Autowired
	private PasswordPolicyResourceService resourceService;
	
	@Autowired
	private PasswordAnalyserService analyserService; 
	
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PasswordPolicyResource> getResources(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return new ResourceList<PasswordPolicyResource>(
				resourceService.getResources(sessionUtils
						.getCurrentRealm(request)));
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/myPasswordPolicys", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PasswordPolicyResource> getResourcesByCurrentPrincipal(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return new ResourceList<PasswordPolicyResource>(
				resourceService.getResources(sessionUtils
						.getPrincipal(request)));
	}
	
	@RequestMapping(value = "passwordPolicys/default", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PasswordPolicyResource> getDefaultPolicy(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, IOException {

		Realm realm;
		if(sessionUtils.hasActiveSession(request)) {
			try(var c = tryAs(sessionUtils.getSession(request),
					sessionUtils.getLocale(request))) {
				realm = getCurrentRealm();
			}
			
		} else {
			realm = realmService.getRealmByHost(request.getServerName());
		}
		
		return new ResourceStatus<PasswordPolicyResource>(resourceService.getDefaultPolicy(
				realm, 
				realm.getResourceCategory()));
	}
	
	@RequestMapping(value = "passwordPolicys/default/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PasswordPolicyResource> getDefaultPolicyForRealm(
			HttpServletRequest request, HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		Realm realm = realmService.getRealmById(id);
		
		return new ResourceStatus<PasswordPolicyResource>(resourceService.getDefaultPolicy(
				realm, 
				realm.getResourceCategory()));
	}
	
	@RequestMapping(value = "passwordPolicys/generate/{id}/{length}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public ResourceStatus<String> generatePassword(
			HttpServletRequest request, HttpServletResponse response, @PathVariable Long id, @PathVariable Integer length)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {
			PasswordPolicyResource policy = resourceService.getResourceById(id);
			return new ResourceStatus<String>(resourceService.generatePassword(policy, length));
		} catch (ResourceNotFoundException e) {
			return new ResourceStatus<String>(false, e.getMessage());
		}
		
	}
	
	@RequestMapping(value = "passwordPolicys/generateNew", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(currentRealmOrDefault = true)
	public ResourceStatus<String> generatePassword(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		PasswordPolicyResource policy = resourceService.getDefaultPasswordPolicy(getCurrentRealm());
		return new ResourceStatus<String>(resourceService.generatePassword(policy, policy.getMinimumLength()));
	}
	
	@RequestMapping(value = "passwordPolicys/myPolicy", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PasswordPolicyResource> getCurrentPrincipalPolicy(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		if (sessionUtils.hasActiveSession(request)) {
			return silentlyCallAs(() -> {
				return doGetCurrentPrincipalPolicy(getCurrentPrincipal());
			}, sessionUtils.getSession(request), sessionUtils.getLocale(request));
		} else {
			AuthenticationState state = AuthenticationState.getCurrentState(request);
			if (state != null && state.getPrincipal() != null) {
				return silentlyCallAs(() -> doGetCurrentPrincipalPolicy(state.getPrincipal()),
						realmService.getRealmByHost(request.getServerName()));
			} else {
				Realm realm = realmService.getRealmByHost(request.getServerName());
				return new ResourceStatus<PasswordPolicyResource>(
						resourceService.getDefaultPolicy(realm, realm.getResourceCategory()));
			}
		}
	}

	protected ResourceStatus<PasswordPolicyResource> doGetCurrentPrincipalPolicy(Principal principal) {
		try {
			return new ResourceStatus<PasswordPolicyResource>(resourceService.resolvePolicy(principal));
		} catch (ResourceNotFoundException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} catch (UnsupportedOperationException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, "Unsupported");
		}
	}
	
	@RequestMapping(value = "passwordPolicys/policy/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PasswordPolicyResource> getPrincipalPolicy(
			HttpServletRequest request, HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		Principal principal = realmService.getPrincipalById(id);
		
		try {
			return new ResourceStatus<PasswordPolicyResource>(resourceService.resolvePolicy(principal));
		} catch (ResourceNotFoundException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} catch (UnsupportedOperationException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, "Unsupported");
		}  
	}
	
	@RequestMapping(value = "passwordPolicys/analyse", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(currentRealmOrDefault = true)
	public ResourceStatus<PasswordPolicyResource> analysePassword(
			HttpServletRequest request, HttpServletResponse response, @RequestParam String password, 
					@RequestParam(required=false) Long id, @RequestParam(required=false) String username)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {
			
			PasswordPolicyResource policy;
			if(id!=null) {
				Principal principal = realmService.getPrincipalById(id);
				policy = resourceService.resolvePolicy(principal);
				username = principal.getPrincipalName();
			} else {
				policy = getCurrentPrincipalPolicy(request, response).getResource();
			}
			
			analyserService.analyse(sessionUtils.getLocale(request), 
					username, 
					HypersocketUtils.urlDecode(password).toCharArray(), 
					policy);
			
			return new ResourceStatus<PasswordPolicyResource>(policy);
		} catch (ResourceException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} catch (PasswordPolicyException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} catch (IOException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		}
	}
	
	@RequestMapping(value = "passwordPolicys/check", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<PasswordPolicyResource> analysePassword(
			HttpServletRequest request, HttpServletResponse response, @RequestParam String username, @RequestParam String password, @RequestParam String dn)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {
			
			PasswordPolicyResource policy = resourceService.getPolicyByDN(dn, realmService.getRealmByHost(request.getServerName()));
			
			analyserService.analyse(sessionUtils.getLocale(request), 
					username, 
					HypersocketUtils.urlDecode(password).toCharArray(), 
					policy);
			
			return new ResourceStatus<PasswordPolicyResource>(policy);
		} catch (IOException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} catch (PasswordPolicyException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		} 
	}

	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return PasswordPolicyResourceColumns.valueOf(col.toUpperCase());
					}

					@Override
					public List<?> getPage(String searchColumn, String searchPattern, int start,
							int length, ColumnSort[] sorting)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.searchResources(
								sessionUtils.getCurrentRealm(request),
								searchColumn, searchPattern, start, length, sorting);
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.getResourceCount(
								sessionUtils.getCurrentRealm(request),
								searchColumn, searchPattern);
					}
				});
	}

	
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		PasswordPolicyResource resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/passwordPolicy/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public PasswordPolicyResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		return resourceService.getResourceById(id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/passwordPolicy", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<PasswordPolicyResource> createOrUpdateResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody AssignableResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		try {

			PasswordPolicyResource newResource;

			Realm realm = sessionUtils.getCurrentRealm(request);

			Set<Role> roles = RoleUtils.processPermissions(resource.getRoles());
			
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			if (resource.getId() != null) {
				newResource = resourceService.updateResource(
						resourceService.getResourceById(resource.getId()),
						resource.getName(), roles, properties);
			} else {
				newResource = resourceService.createResource(
						resource.getName(), roles,
						realm, properties);
			}
			return new ResourceStatus<PasswordPolicyResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							PasswordPolicyResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<PasswordPolicyResource>(false,
					e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/passwordPolicy/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<PasswordPolicyResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			PasswordPolicyResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<PasswordPolicyResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								PasswordPolicyResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<PasswordPolicyResource>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					PasswordPolicyResourceServiceImpl.RESOURCE_BUNDLE,
					"resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<PasswordPolicyResource>(false, e.getMessage());
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "passwordPolicys/personal", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> personalResources(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return ResourceColumns.valueOf(col);
					}

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start, int length,
							ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
						return resourceService.searchPersonalResources(sessionUtils.getPrincipal(request),
								searchColumn, searchPattern, start, length, sorting);
					}
					
					@Override
					public Long getTotalCount(String searchColumn, String searchPattern) throws UnauthorizedException, AccessDeniedException {
						return resourceService.getPersonalResourceCount(
								sessionUtils.getPrincipal(request),
								searchColumn, searchPattern);
					}
				});
	}
}
