package com.hypersocket.attributes.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.attributes.user.UserAttributeCategoryColumns;
import com.hypersocket.attributes.user.UserAttributeCategoryService;
import com.hypersocket.attributes.user.UserAttributeServiceImpl;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class UserAttributeCategoryController extends ResourceController {

	@Autowired
	UserAttributeCategoryService service;

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableAttributeCategories(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return UserAttributeCategoryColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return service.searchResources(sessionUtils.getCurrentRealm(request),
									searchColumn, searchPattern, start, length, sorting);

						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return service
									.getResourceCount(sessionUtils.getCurrentRealm(request), searchColumn, searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/userAttributeCategory", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserAttributeCategory> createOrUpdateAttributeCategory(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserAttributeCategoryUpdate attributeCategory)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {

			UserAttributeCategory newAttributeCategory;

			if (attributeCategory.getId() != null) {
				newAttributeCategory = service.updateAttributeCategory(service
						.getResourceById(attributeCategory.getId()),
						attributeCategory.getName(), attributeCategory
								.getWeight());
			} else {
				newAttributeCategory = service.createAttributeCategory(
						attributeCategory.getName(),
						attributeCategory.getWeight());
			}

			return new ResourceStatus<UserAttributeCategory>(
					newAttributeCategory,
					I18N.getResource(
							sessionUtils.getLocale(request),
							UserAttributeServiceImpl.RESOURCE_BUNDLE,
							attributeCategory.getId() != null ? "attributeCategory.updated.info"
									: "attributeCategory.created.info",
							attributeCategory.getName()));
		} catch (ResourceException e) {
			return new ResourceStatus<UserAttributeCategory>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	
	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/userAttributeCategory/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<UserAttributeCategory> deleteAttributeCategory(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			UserAttributeCategory category = service.getResourceById(id);

			if (category == null) {
				return new ResourceStatus<UserAttributeCategory>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								UserAttributeServiceImpl.RESOURCE_BUNDLE,
								"error.invalidAttributeCategoryId", id));
			}

			String preDeletedName = category.getName();
			try {
				service.deleteAttributeCategory(category);
			} catch (DataIntegrityViolationException e) {
				return new ResourceStatus<UserAttributeCategory>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								UserAttributeServiceImpl.RESOURCE_BUNDLE,
								"attributeCategory.deleteError.info",
								preDeletedName));
			} 

			return new ResourceStatus<UserAttributeCategory>(true,
					I18N.getResource(sessionUtils.getLocale(request),
							UserAttributeServiceImpl.RESOURCE_BUNDLE,
							"attributeCategory.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<UserAttributeCategory>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/categories", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getCategories(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			List<SelectOption> result = new ArrayList<SelectOption>();
			for (UserAttributeCategory category : service.getResources(sessionUtils.getCurrentRealm(request))) {
				result.add(new SelectOption(Long.toString(category.getId()),
						category.getName()));
			}
			return new ResourceList<SelectOption>(result);
		} finally {
			clearAuthenticatedContext();
		}
	}
}
