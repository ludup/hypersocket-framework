package com.hypersocket.attributes.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.attributes.AttributeCategoryService;
import com.hypersocket.attributes.RealmAttributeCategory;
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
public abstract class AbstractAttributeCategoryController<A extends AbstractAttribute<T>, T extends RealmAttributeCategory<A>>
		extends ResourceController {

	protected AttributeCategoryService<A, T> service;

	private String resourceBundle;

	protected AbstractAttributeCategoryController(String resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	protected BootstrapTableResult<?> tableAttributeCategories(final HttpServletRequest request)
			throws UnauthorizedException, SessionTimeoutException, AccessDeniedException {
		return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

			@Override
			public Column getColumn(String col) {
				return AbstractAttributeCategoryController.this.getColumn(col);
			}

			@Override
			public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
					ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
				return service.searchResources(sessionUtils.getCurrentRealm(request), searchColumn, searchPattern,
						start, length, sorting);

			}

			@Override
			public Long getTotalCount(String searchColumn, String searchPattern)
					throws UnauthorizedException, AccessDeniedException {
				return service.getResourceCount(sessionUtils.getCurrentRealm(request), searchColumn, searchPattern);
			}
		});
	}

	protected ResourceStatus<T> createOrUpdateAttributeCategory(HttpServletRequest request,
			AbstractCategoryUpdate attributeCategory)
			throws UnauthorizedException, SessionTimeoutException, AccessDeniedException {
		try {

			T newAttributeCategory;

			if (attributeCategory.getId() != null) {
				newAttributeCategory = service.updateAttributeCategory(
						service.getResourceById(attributeCategory.getId()), attributeCategory.getName(),
						attributeCategory.getWeight());
			} else {
				newAttributeCategory = service.createAttributeCategory(attributeCategory.getName(),
						attributeCategory.getWeight());
			}

			return new ResourceStatus<T>(newAttributeCategory,
					I18N.getResource(
							sessionUtils.getLocale(request), resourceBundle, attributeCategory.getId() != null
									? "attributeCategory.updated.info" : "attributeCategory.created.info",
							attributeCategory.getName()));
		} catch (ResourceException e) {
			return new ResourceStatus<T>(false, e.getMessage());
		} 
	}

	protected ResourceStatus<T> deleteAttributeCategory(HttpServletRequest request, Long id)
			throws UnauthorizedException, SessionTimeoutException, AccessDeniedException {
		try {

			T category = service.getResourceById(id);

			if (category == null) {
				return new ResourceStatus<T>(false, I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
						"error.invalidAttributeCategoryId", id));
			}

			String preDeletedName = category.getName();
			try {
				service.deleteAttributeCategory(category);
			} catch (DataIntegrityViolationException e) {
				return new ResourceStatus<T>(false, I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
						"attributeCategory.deleteError.info", preDeletedName));
			}

			return new ResourceStatus<T>(true, I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
					"attributeCategory.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<T>(false, e.getMessage());
		} 
	}

	protected ResourceList<SelectOption> getCategories(HttpServletRequest request)
		throws UnauthorizedException, SessionTimeoutException, AccessDeniedException {
		List<SelectOption> result = new ArrayList<SelectOption>();
		for (T category : service.getResources(sessionUtils.getCurrentRealm(request))) {
			result.add(new SelectOption(Long.toString(category.getId()), category.getName()));
		}
		return new ResourceList<SelectOption>(result);
	}

	protected abstract Column getColumn(String col);

}