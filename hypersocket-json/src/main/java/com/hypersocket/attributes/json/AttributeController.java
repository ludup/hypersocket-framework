package com.hypersocket.attributes.json;

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

import com.hypersocket.attributes.Attribute;
import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.attributes.AttributeColumns;
import com.hypersocket.attributes.AttributeService;
import com.hypersocket.attributes.AttributeServiceImpl;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DataTablesResult;
import com.hypersocket.tables.json.DataTablesPageProcessor;

@Controller
public class AttributeController extends ResourceController {

	@Autowired
	AttributeService service;

	@AuthenticationRequired
	@RequestMapping(value = "attributes/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public DataTablesResult tableAttributes(final HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return processDataTablesRequest(request,
					new DataTablesPageProcessor() {

						@Override
						public Column getColumn(int col) {
							return AttributeColumns.values()[col];
						}

						@Override
						public List<?> getPage(String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return service.searchAttributes(searchPattern,
									start, length, sorting);

						}

						@Override
						public Long getTotalCount(String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return service.getAttributeCount(searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "attributeCategories/attributeCategory", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<AttributeCategory> createOrUpdateAttributeCategory(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody AttributeCategoryUpdate attributeCategory)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			AttributeCategory newAttributeCategory;

			try {
				newAttributeCategory = service.createAttributeCategory(
						attributeCategory.getName(),
						attributeCategory.getContext(),
						attributeCategory.getWeight());

				return new ResourceStatus<AttributeCategory>(
						newAttributeCategory,
						I18N.getResource(
								sessionUtils.getLocale(request),
								AttributeServiceImpl.RESOURCE_BUNDLE,
								attributeCategory.getId() != null ? "attributeCategory.updated.info"
										: "attributeCategory.created.info",
								attributeCategory.getName()));
			} catch (ResourceCreationException e) {
				return new ResourceStatus<AttributeCategory>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								e.getBundle(), e.getResourceKey(), e.getArgs()));
			}
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "attributes/attribute", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Attribute> createOrUpdateAttribute(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody AttributeUpdate attribute)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Attribute newAttribute;

			if (attribute.getId() != null) {
				newAttribute = service.updateAttribute(
						service.getAttributeById(attribute.getId()),
						attribute.getName(), attribute.getCategory(),
						attribute.getDescription(),
						attribute.getDefaultValue(), attribute.getWeight(),
						attribute.getType(), attribute.getReadOnly(),
						attribute.getEncrypted(), attribute.getVariableName());
			} else {
				newAttribute = service.createAttribute(attribute.getName(),
						attribute.getCategory(), attribute.getDescription(),
						attribute.getDefaultValue(), attribute.getWeight(),
						attribute.getType(), attribute.getReadOnly(),
						attribute.getEncrypted(), attribute.getVariableName());
			}
			return new ResourceStatus<Attribute>(newAttribute,
					I18N.getResource(sessionUtils.getLocale(request),
							AttributeServiceImpl.RESOURCE_BUNDLE, attribute
									.getId() != null ? "attribute.updated.info"
									: "attribute.created.info", attribute
									.getName()));
		} catch (ResourceCreationException e) {
			return new ResourceStatus<Attribute>(false, I18N.getResource(
					sessionUtils.getLocale(request), e.getBundle(),
					e.getResourceKey(), e.getArgs()));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "attributes/attribute/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Attribute> deleteAttribute(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			Attribute attribute = service.getAttributeById(id);

			if (attribute == null) {
				return new ResourceStatus<Attribute>(false, I18N.getResource(
						sessionUtils.getLocale(request),
						AttributeServiceImpl.RESOURCE_BUNDLE,
						"error.invalidAttributeId", id));
			}

			String preDeletedName = attribute.getName();
			service.deleteAttribute(attribute);

			return new ResourceStatus<Attribute>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					AttributeServiceImpl.RESOURCE_BUNDLE,
					"attribute.deleted.info", preDeletedName));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "attributeCategories/categories", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getActions(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			List<SelectOption> result = new ArrayList<SelectOption>();
			for (AttributeCategory category : service.getCategories()) {
				result.add(new SelectOption(Long.toString(category.getId()),
						category.getName()));
			}
			return new ResourceList<SelectOption>(result);
		} finally {
			clearAuthenticatedContext();
		}
	}
}
