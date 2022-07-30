package com.hypersocket.attributes.json;

import javax.annotation.PostConstruct;
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

import com.hypersocket.attributes.role.RoleAttribute;
import com.hypersocket.attributes.role.RoleAttributeCategory;
import com.hypersocket.attributes.role.RoleAttributeCategoryColumns;
import com.hypersocket.attributes.role.RoleAttributeCategoryService;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;

@Controller
public class RoleAttributeCategoryController extends AbstractAttributeCategoryController<RoleAttribute, RoleAttributeCategory> {

	@Autowired
	private RoleAttributeCategoryService roleAttributeCategoryService;
	
	@PostConstruct
	private void init() {
		service = roleAttributeCategoryService;
	}
	
	public RoleAttributeCategoryController() {
		super("RoleAttributes");
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributeCategories/table", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableAttributeCategories(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.tableAttributeCategories(request);
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributeCategories/roleAttributeCategory", method = RequestMethod.POST, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<RoleAttributeCategory> createOrUpdateAttributeCategory(HttpServletRequest request,
			HttpServletResponse response, @RequestBody AbstractCategoryUpdate attributeCategory)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.createOrUpdateAttributeCategory(request, attributeCategory);
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributeCategories/roleAttributeCategory/{id}", method = RequestMethod.DELETE, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<RoleAttributeCategory> deleteAttributeCategory(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.deleteAttributeCategory(request, id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributeCategories/categories", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<SelectOption> getCategories(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.getCategories(request);
	}

	@Override
	protected Column getColumn(String col) {
		return RoleAttributeCategoryColumns.valueOf(col.toUpperCase());
	}
}
