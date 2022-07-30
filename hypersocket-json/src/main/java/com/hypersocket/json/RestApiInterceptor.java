/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import java.util.Arrays;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserCredentials;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

@Component
public class RestApiInterceptor extends HandlerInterceptorAdapter {

	private static Logger log = LoggerFactory.getLogger(RestApiInterceptor.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private LocalUserRepository userRepository;

	@Autowired
	private PasswordEncryptionService encryptionService;

	@Override
	@SuppressWarnings({ "unchecked", "deprecation" })
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
						request.setAttribute(RestApi.API_USER, user);
						Session session = getCurrentSession(request, username);
						if(session == null){
							AuthenticationScheme authenticationScheme = authenticationService.getSchemeByResourceKey(realm, "basic");
							session = sessionService.openSession(request.getRemoteAddr(), user, authenticationScheme, RestApi.API_REST,
									MapUtils.transformedMap(request.getParameterMap(), new Transformer() {
										@Override
										public Object transform(Object o) {
											return o;
										}
									}, new Transformer() {
										@Override
										public Object transform(Object o) {
											return Arrays.toString((Object[]) o);
										}
									}), realm);
							sessionService.registerNonCookieSession(request.getRemoteAddr(), username, RestApi.HTTP_BASIC_AUTH_SCHEME, session);
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

	@SuppressWarnings("deprecation")
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		authenticationService.clearPrincipalContext();
	}

	private Session getCurrentSession(HttpServletRequest request, String username)  {
		try {
			String usernameEncoded = Base64.toBase64String(username.getBytes());
			return sessionService.getNonCookieSession(request.getRemoteAddr(), usernameEncoded, RestApi.HTTP_BASIC_AUTH_SCHEME);
		}catch (AccessDeniedException e){
			return null;
		}
	}

	private boolean verify(LocalUser user, String password) {
		LocalUserCredentials creds = userRepository.getCredentials(user);

		try {
			return encryptionService.authenticate(password.toCharArray(),
					Base64.decode(creds.getEncodedPassword()), Base64.decode(creds.getEncodedSalt()),
					creds.getEncryptionType());

		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.error("Failed to verify password", e);
			}
			return false;
		}
	}
}
