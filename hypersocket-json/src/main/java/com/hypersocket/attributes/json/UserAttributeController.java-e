package com.hypersocket.attributes.json;

import java.util.HashSet;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeColumns;
import com.hypersocket.attributes.user.UserAttributeService;
import com.hypersocket.attributes.user.UserAttributeServiceImpl;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class UserAttributeController extends ResourceController {

	@Autowired
	UserAttributeService service;

	@AuthenticationRequired
	@RequestMapping(value = "userAttributes/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableAttributes(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return UserAttributeColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return service.searchResources(sessionUtils.getCurrentRealm(request),
									searchColumn, searchPattern,
									start, length, sorting);

						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return service.getResourceCount(sessionUtils.getCurrentRealm(request), 
									searchColumn, searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributes/userAttribute", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserAttribute> createOrUpdateAttribute(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserAttributeUpdate attribute)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			UserAttribute newAttribute;

			Set<Role> roles = new HashSet<Role>();
			for (Long id : attribute.getRoles()) {
				roles.add(permissionRepository.getRoleById(id));
			}
			
			if (attribute.getId() != null) {
				newAttribute = service.updateAttribute(
						service.getResourceById(attribute.getId()),
						attribute.getName(), attribute.getCategory(),
						attribute.getDescription(),
						attribute.getDefaultValue(), attribute.getWeight(),
						attribute.getType(), attribute.getDisplayMode(), attribute.getReadOnly(),
						attribute.getEncrypted(), attribute.getVariableName(), roles);
			} else {
				newAttribute = service.createAttribute(attribute.getName(),
						attribute.getCategory(), attribute.getDescription(),
						attribute.getDefaultValue(), attribute.getWeight(),
						attribute.getType(), attribute.getDisplayMode(), attribute.getReadOnly(),
						attribute.getEncrypted(), attribute.getVariableName(), roles);
			}
			return new ResourceStatus<UserAttribute>(newAttribute,
					I18N.getResource(sessionUtils.getLocale(request),
							UserAttributeServiceImpl.RESOURCE_BUNDLE, attribute
									.getId() != null ? "attribute.updated.info"
									: "attribute.created.info", attribute
									.getName()));
		} catch (ResourceException e) {
			return new ResourceStatus<UserAttribute>(false, e.getMessage());

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributes/userAttribute/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserAttribute> deleteAttribute(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			UserAttribute attribute = service.getResourceById(id);

			if (attribute == null) {
				return new ResourceStatus<UserAttribute>(false, I18N.getResource(
						sessionUtils.getLocale(request),
						UserAttributeServiceImpl.RESOURCE_BUNDLE,
						"error.invalidAttributeId", id));
			}

			String preDeletedName = attribute.getName();
			service.deleteAttribute(attribute);

			return new ResourceStatus<UserAttribute>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					UserAttributeServiceImpl.RESOURCE_BUNDLE,
					"attribute.deleted.info", preDeletedName));

		} catch(ResourceException ex) { 
			return new ResourceStatus<UserAttribute>(false, ex.getMessage());
		}
		finally {
			clearAuthenticatedContext();
		}
	}
}
