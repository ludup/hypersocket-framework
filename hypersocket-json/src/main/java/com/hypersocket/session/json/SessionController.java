package com.hypersocket.session.json;

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
import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.AuthenticationSuccessResult;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.Session;

@Controller
public class SessionController extends AuthenticatedController {

	@RequestMapping(value = "session/touch", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult touch(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return getSuccessfulResult(
					sessionUtils.touchSession(request, response));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "session/peek", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public AuthenticationResult peek(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		
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
	public AuthenticationResult switchRealm(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws UnauthorizedException, AccessDeniedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			Session session = sessionUtils.getActiveSession(request);

			Realm realm = realmService.getRealmById(id);

			if (realm == null) {
				throw new ResourceNotFoundException(AuthenticationService.RESOURCE_BUNDLE,
						"error.invalidRealm", id);
			}

			sessionService.switchRealm(session, realm);

			return getSuccessfulResult(session, I18N.getResource(
					sessionUtils.getLocale(request), AuthenticationService.RESOURCE_BUNDLE,
					"info.inRealm", realm.getName()), "");
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "session/switchLanguage/{lang}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus switchLanguage(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("lang") String locale)
			throws UnauthorizedException, AccessDeniedException,
			ResourceNotFoundException {
		sessionUtils.setLocale(request, response, locale);
		return new RequestStatus();
	}

	private AuthenticationResult getSuccessfulResult(Session session,
			String info, String homePage) {
		return new AuthenticationSuccessResult(info,
				i18nService.hasUserLocales(), session, homePage);
	}
	
	private AuthenticationResult getSuccessfulResult(Session session) {
		return new AuthenticationSuccessResult("",
				i18nService.hasUserLocales(), session, "");
	}
}
