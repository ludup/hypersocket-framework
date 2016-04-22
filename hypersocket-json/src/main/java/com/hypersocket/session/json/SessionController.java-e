package com.hypersocket.session.json;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.AuthenticationSuccessResult;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionColumns;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.SessionServiceImpl;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class SessionController extends ResourceController {

	@RequestMapping(value = "session/touch", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult touch(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return getSuccessfulResult(sessionUtils.touchSession(request, response));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "session/peek", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult peek(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return getSuccessfulResult(sessionUtils.getSession(request));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "session/switchRealm/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult switchRealm(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws UnauthorizedException, AccessDeniedException, ResourceNotFoundException,
					SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.getActiveSession(request);

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				throw new ResourceNotFoundException(AuthenticationService.RESOURCE_BUNDLE, "error.invalidRealm", id);
			}

			sessionService.switchRealm(session, realm);

			return getSuccessfulResult(session, "info=" + I18N.getResource(sessionUtils.getLocale(request),
					AuthenticationService.RESOURCE_BUNDLE, "info.inRealm", realm.getName()), "");
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "session/switchLanguage/{lang}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus switchLanguage(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("lang") String locale)
					throws UnauthorizedException, AccessDeniedException, ResourceNotFoundException {
		sessionUtils.setLocale(request, response, locale);
		return new RequestStatus();
	}

	private AuthenticationResult getSuccessfulResult(Session session, String info, String homePage) {
		return new AuthenticationSuccessResult(info, configurationService.hasUserLocales(), session, homePage);
	}

	private AuthenticationResult getSuccessfulResult(Session session) {
		return new AuthenticationSuccessResult("", configurationService.hasUserLocales(), session, "");
	}

	@RequestMapping(value = "session/flash/{type}/{msg}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus flashMessage(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String type, @PathVariable String msg)
					throws UnauthorizedException, AccessDeniedException, ResourceNotFoundException {
		request.getSession().setAttribute("flash", type + "=" + msg);
		return new RequestStatus();
	}

	@AuthenticationRequired
	@RequestMapping(value = "session/impersonate/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus impersonateUser(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws UnauthorizedException, AccessDeniedException, ResourceNotFoundException,
					SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.getActiveSession(request);

			Principal principal = realmService.getPrincipalById(sessionUtils.getCurrentRealm(request), id);

			if (principal == null) {
				throw new ResourceNotFoundException(AuthenticationService.RESOURCE_BUNDLE, "error.invalidPrincipal",
						id);
			}

			sessionService.switchPrincipal(session, principal);

			request.getSession().setAttribute("flash", "success=" + I18N.getResource(sessionUtils.getLocale(request),
					SessionService.RESOURCE_BUNDLE, "info.impersonatingPrincipal", principal.getName()));

			return new RequestStatus();
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "session/logoff/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus logoffUser(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") String id) throws UnauthorizedException, AccessDeniedException,
					ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Session currentSession = sessionUtils.getActiveSession(request);
			Session logoffSession = sessionService.getSession(id);
			if (currentSession.equals(logoffSession)) {
				return new RequestStatus(false, "error.cannotRemoveCurrentSession");
			}

			sessionService.closeSession(logoffSession);

			return new RequestStatus(true,
					I18N.getResource(sessionUtils.getLocale(request), SessionServiceImpl.RESOURCE_BUNDLE,
							"info.sessionClosed", logoffSession.getCurrentPrincipal().getName()));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "session/revert", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus impersonateEnd(HttpServletRequest request, HttpServletResponse response)
			throws UnauthorizedException, AccessDeniedException, ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.getActiveSession(request);

			sessionService.revertPrincipal(session);

			request.getSession().setAttribute("flash", "success=" + I18N.getResource(sessionUtils.getLocale(request),
					SessionService.RESOURCE_BUNDLE, "info.revertedPrincipal", session.getCurrentPrincipal().getName()));

			return new RequestStatus();
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "session/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableResources(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request), sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

				@Override
				public Column getColumn(String col) {
					return SessionColumns.valueOf(col.toUpperCase());
				}

				@Override
				public List<?> getPage(String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting)
						throws UnauthorizedException, AccessDeniedException {
					return sessionService.searchResources(sessionUtils.getCurrentRealm(request), searchPattern, start,
							length, sorting);
				}

				@Override
				public Long getTotalCount(String searchColumn, String searchPattern) throws UnauthorizedException, AccessDeniedException {
					return sessionService.getResourceCount(sessionUtils.getCurrentRealm(request), searchPattern);
				}
			});
		} finally {
			clearAuthenticatedContext();
		}
	}

}
