package com.hypersocket.browser.json;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.browser.BrowserLaunchable;
import com.hypersocket.browser.BrowserLaunchableColumns;
import com.hypersocket.browser.BrowserLaunchableService;
import com.hypersocket.browser.BrowserLaunchableServiceImpl;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTableController;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class BrowserLaunchableController extends BootstrapTableController<Void> {

	@Autowired
	private BrowserLaunchableService resourceService;

	@Autowired
	private SessionUtils sessionUtils;
	
	@AuthenticationRequired
	@RequestMapping(value = "browser/fingerprint", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<String> getFingerprint(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceStatus<String>(true, resourceService.getFingerprint());
	}

	@AuthenticationRequired
	@RequestMapping(value = "browser/myResources", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<BrowserLaunchable> myBrowserResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		ResourceList<BrowserLaunchable> list = new ResourceList<BrowserLaunchable>(
				new HashMap<String,String>(),
				resourceService.getPersonalResources(sessionUtils
						.getPrincipal(request)));
		return list;
	}

	@AuthenticationRequired
	@RequestMapping(value = "browser/personal", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableNetworkResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
		SessionTimeoutException {
		return processDataTablesRequest(request,
				new BootstrapTablePageProcessor() {

					@Override
					public Column getColumn(String col) {
						return BrowserLaunchableColumns.valueOf(col.toUpperCase());
					}

					@Override
					public List<?> getPage(String searchColumn, String searchPattern, int start,
							int length, ColumnSort[] sorting)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.searchPersonalResources(
								sessionUtils.getPrincipal(request),
								searchPattern, start, length, sorting);
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return resourceService.getPersonalResourceCount(
								sessionUtils.getPrincipal(request),
								searchPattern);
					}
				});
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "browser/browser/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<BrowserLaunchable> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		try {

			resourceService.deleteResource(id);
			return new ResourceStatus<BrowserLaunchable>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					BrowserLaunchableServiceImpl.RESOURCE_BUNDLE,
					"resource.deleted.info"));
		} catch (ResourceException e) {
			return new ResourceStatus<BrowserLaunchable>(false, e.getMessage());
		} 
	}
}
