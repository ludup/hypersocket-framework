/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserCredentials;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.UUID;

@Component
public class RestApiInterceptor extends HandlerInterceptorAdapter {

	private static Logger log = LoggerFactory.getLogger(RestApiInterceptor.class);

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	SessionService sessionService;

	@Autowired
	RealmService realmService;

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	PasswordEncryptionService encryptionService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		request.setAttribute(RestApi.API_REST, RestApi.API_REST);
		String authorizationHeader = request.getHeader(RestApi.HTTP_HEADER_AUTH);
		if(StringUtils.isNotBlank(authorizationHeader)){
			if (authorizationHeader.toLowerCase().startsWith(RestApi.HTTP_BASIC_AUTH_SCHEME)) {
				String[] parts = new String(Base64.decode(authorizationHeader.substring(6))).split(":");
				if (parts.length == 2) {
					String username = parts[0];
					String password = parts[1];

					Realm realm = realmService.getSystemRealm();
					LocalUser user = userRepository.getUserByName(username, realm);

					if(verify(user, password)){
						request.setAttribute(RestApi.API_USER, username);
						Session session = getCurrentSession(request, authorizationHeader);
						if(session == null){
							session = createRestApiSession(user, request.getRemoteAddr());
							sessionService.registerNonCookieSession(request.getRemoteAddr(), authorizationHeader,
									RestApi.HTTP_BASIC_AUTH_SCHEME, session);
						}
						authenticationService.setCurrentSession(session, realm, user, Locale.ENGLISH);
						return true;
					}
				}
			}
		}

		response.addHeader(RestApi.HTTP_HEADER_WWW_AUTHENTICATE, String.format("Basic realm=\"%s\"", RestApi.API_REST));
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not Authorized");

		return false;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		authenticationService.clearPrincipalContext();
	}

	private Session createRestApiSession(Principal principal, String remoteAddress) {
		Session session = new Session();
		session.setId(UUID.randomUUID().toString());
		session.setCurrentRealm(realmService.getSystemRealm());
		session.setOs(System.getProperty("os.name"));
		session.setOsVersion(System.getProperty("os.version"));
		try {
			FieldUtils.writeField(session, "principal", principal,true);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		session.setUserAgent("N/A");
		session.setUserAgentVersion("N/A");
		session.setRemoteAddress(remoteAddress);
		return session;
	}

	private Session getCurrentSession(HttpServletRequest request, String authorizationHeader)  {
		try {
			return sessionService.getNonCookieSession(request.getRemoteAddr(), authorizationHeader, RestApi.HTTP_BASIC_AUTH_SCHEME);
		}catch (AccessDeniedException e){
			return null;
		}
	}

	private boolean verify(LocalUser user, String password) {
		LocalUserCredentials creds = userRepository.getCredentials(user);

		try {
			return encryptionService.authenticate(password.toCharArray(),
					creds.getPassword(), creds.getSalt(),
					creds.getEncryptionType());

		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.error("Failed to verify password", e);
			}
			return false;
		}
	}
}
