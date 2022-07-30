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

import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.attributes.user.UserAttributeCategoryColumns;
import com.hypersocket.attributes.user.UserAttributeCategoryService;
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
public class UserAttributeCategoryController extends AbstractAttributeCategoryController<UserAttribute, UserAttributeCategory> {

	@Autowired
	private UserAttributeCategoryService userAttributeCategoryService;
	
	@PostConstruct
	private void init() {
		service = userAttributeCategoryService;
	}
	
	public UserAttributeCategoryController() {
		super("UserAttributes");
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/table", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableAttributeCategories(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.tableAttributeCategories(request);
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/userAttributeCategory", method = RequestMethod.POST, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<UserAttributeCategory> createOrUpdateAttributeCategory(HttpServletRequest request,
			HttpServletResponse response, @RequestBody UserAttributeCategoryUpdate attributeCategory)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.createOrUpdateAttributeCategory(request, attributeCategory);
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/userAttributeCategory/{id}", method = RequestMethod.DELETE, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<UserAttributeCategory> deleteAttributeCategory(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.deleteAttributeCategory(request, id);
	}

	@AuthenticationRequired
	@RequestMapping(value = "userAttributeCategories/categories", method = RequestMethod.GET, produces = {
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
		return UserAttributeCategoryColumns.valueOf(col.toUpperCase());
	}
}
