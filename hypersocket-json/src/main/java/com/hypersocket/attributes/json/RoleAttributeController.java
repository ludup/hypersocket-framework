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
import com.hypersocket.attributes.role.RoleAttributeColumns;
import com.hypersocket.attributes.role.RoleAttributeService;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;

@Controller
public class RoleAttributeController extends AbstractAttributeController<RoleAttribute, RoleAttributeCategory> {

	@Autowired
	private RoleAttributeService roleAttributeService;

	public RoleAttributeController() {
		super("RoleAttributes", "attribute");
	}

	@PostConstruct
	private void init() {
		service = roleAttributeService;
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributes/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableAttributes(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.tableAttributes(request);
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributes/roleAttribute", method = RequestMethod.POST, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<RoleAttribute> createOrUpdateAttribute(HttpServletRequest request,
			HttpServletResponse response, @RequestBody AttributeUpdate attribute)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.createOrUpdateAttribute(request, response, attribute);
	}

	@AuthenticationRequired
	@RequestMapping(value = "roleAttributes/roleAttribute/{id}", method = RequestMethod.DELETE, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<RoleAttribute> deleteAttribute(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return super.deleteAttribute(request, response, id);
	}

	@Override
	public Column getColumn(String col) {
		return RoleAttributeColumns.valueOf(col.toUpperCase());
	}
}
