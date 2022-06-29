/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;

public class AuthenticationState {

	private static final String PREVIOUS_AUTHENTICATION_SCHEME = "previousAuthenticationScheme";
	private static final String AUTHENTICATION_STATE = "authenticationState";
	private static final String COOKIES = "cookies";
	
	private Set<String> completedModules = new HashSet<>();
	private Stack<AuthenticationScheme> previousSchemes = new Stack<>();
	private Stack<Integer> previousIndex = new Stack<>();
	private Stack<List<AuthenticationModule>> previousModules = new Stack<>();
	private AuthenticationScheme scheme;
	private AuthenticationScheme initialScheme;
	private String remoteAddress;
	private Integer currentIndex = 0;
	private List<AuthenticationModule> modules;
	private List<PostAuthenticationStep> sessionPostAuthenticationSteps = new ArrayList<PostAuthenticationStep>();
	private List<PostAuthenticationStep> nonSessionPostAuthenticationSteps = new ArrayList<PostAuthenticationStep>();
	private String lastErrorMsg;
	private boolean lastErrorIsResourceKey;
	private String lastErrorType = "error";
	private String lastPrincipalName;
	private String lastRealmName;
	private Realm realm;
	private Realm hostRealm;
	private Principal principal;
	private Principal lastPrincipal;
	private Session session;
	private int attempts = 0;
	private String homePage = "";
	private boolean hasEnded = false;
	private List<AuthenticationStateListener> listeners = new ArrayList<AuthenticationStateListener>();
	private Locale locale;
	private Map<String, String> parameters = new HashMap<String, String>();
	private Map<String, Object> environment = new HashMap<String, Object>();
	private Map<String, String[]> requestParameters = new HashMap<String, String[]>();
	private PrincipalType principalType = PrincipalType.USER;
	
	AuthenticationState(String remoteAddress, Map<String,Object> environment, Locale locale) {
		this.remoteAddress = remoteAddress;
		this.environment = environment;
		this.locale = locale;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Cookie> getCookies() {
		return (Map<String, Cookie>) environment.get(AuthenticationState.COOKIES);
	}

	public Locale getLocale() {
		return locale;
	}
	
	public String getInitialSchemeResourceKey() {
		return initialScheme.getResourceKey();
	}
	
	public AuthenticationScheme getInitialScheme() {
		return initialScheme;
	}
	
	public void setInitialScheme(AuthenticationScheme initialScheme) {
		this.initialScheme = initialScheme;
	}
	
	public PrincipalType getPrincipalType() {
		return principalType;
	}

	public void setPrincipalType(PrincipalType principalType) {
		this.principalType = principalType;
	}

	public boolean isPrimaryState() {
		return previousSchemes.isEmpty();
	}
	
	public AuthenticationModule getCurrentModule() {
		if (currentIndex >= modules.size())
			throw new IllegalStateException(
					"Current index is greater than the number of modules");
		return modules.get(currentIndex);
	}

	public void clean() {
		currentIndex = 0;
		attempts = 0;
		lastPrincipal = null;
		lastPrincipalName = null;
		lastRealmName = null;
		lastErrorMsg = null;
		lastErrorIsResourceKey = false;
		principal = null;
		parameters.clear();
	}
	
	public void switchScheme(AuthenticationScheme scheme, List<AuthenticationModule> modules) {
		
		this.previousIndex.push(currentIndex);
		this.previousSchemes.push(this.scheme);
		this.previousModules.push(this.modules);
		
		this.scheme = scheme;
		this.modules = modules;
		currentIndex = 0;
	}
	
	public void switchBack() {
		
		this.currentIndex = previousIndex.pop();
		this.scheme = previousSchemes.pop();
		this.modules = previousModules.pop();
		
		if(!isPrimaryState() && isAuthenticationComplete()) {
			switchBack();
		}
		
	}
	
	public void addListener(AuthenticationStateListener listener) {
		listeners.add(listener);
	}
	
	public void complete(Session session) {
		for(AuthenticationStateListener listener : listeners) {
			listener.logonComplete(session);
		}
	}
	
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public boolean isNew() {
		return attempts < 1;
	}

	public Integer getCurrentIndex() {
		return currentIndex;
	}

	public boolean isAuthenticationComplete() {
		return currentIndex >= modules.size();
	}

	public Set<String> getCompletedModules() {
		return Collections.unmodifiableSet(completedModules);
	}
	
	public void nextModule() {
		completedModules.add(getCurrentModule().getTemplate());
		this.currentIndex++;
	}
	
	void revertModule() {
		if(currentIndex > 0) {
			this.currentIndex--;
		}
	}

	public AuthenticationScheme getScheme() {
		return scheme;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public List<AuthenticationModule> getModules() {
		return modules;
	}

	public String getLastErrorMsg() {
		return lastErrorMsg;
	}

	public void setLastErrorMsg(String lastErrorMsg) {
		this.lastErrorMsg = lastErrorMsg;
	}

	public String getLastErrorType() {
		return lastErrorType;
	}

	public void setLastErrorType(String lastErrorType) {
		this.lastErrorType = lastErrorType;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
		this.hostRealm = null;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void addPostAuthenticationStep(PostAuthenticationStep proc) throws AccessDeniedException {
		if(proc.requiresSession(this)) {
			sessionPostAuthenticationSteps.add(proc);
		} else {
			nonSessionPostAuthenticationSteps.add(proc);
		}
	}

	public boolean hasPostAuthenticationStep() {
		return (sessionPostAuthenticationSteps.size() + nonSessionPostAuthenticationSteps.size()) > 0;
	}
	
	public boolean canCreateSession() {
		return isAuthenticationComplete() && nonSessionPostAuthenticationSteps.isEmpty() && session==null;
	}

	public PostAuthenticationStep getCurrentPostAuthenticationStep() {
		if (!hasPostAuthenticationStep()) {
			return null;
		}
		if(nonSessionPostAuthenticationSteps.size() > 0) {
			return nonSessionPostAuthenticationSteps.get(0);
		} else {
			return sessionPostAuthenticationSteps.get(0);
		}
	}

	public void nextPostAuthenticationStep() {
		if(nonSessionPostAuthenticationSteps.size() > 0) {
			nonSessionPostAuthenticationSteps.remove(0);
		} else {
			sessionPostAuthenticationSteps.remove(0);
		}
	}

	public boolean hasParameter(String name) {
		return parameters.containsKey(name);
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public void setScheme(AuthenticationScheme scheme) {
		this.scheme = scheme;
	}

	public void setModules(List<AuthenticationModule> modules) {
		this.modules = modules;
	}
	
	public void setLastErrorIsResourceKey(boolean lastErrorIsResourceKey) {
		this.lastErrorIsResourceKey = lastErrorIsResourceKey;
	}
	
	public boolean getLastErrorIsResourceKey() {
		return lastErrorIsResourceKey;
	}

	public void setLastPrincipalName(String username) {
		this.lastPrincipalName = username;
	}

	public void setLastRealmName(String realmName) {
		this.lastRealmName = realmName;
	}
	
	public String getLastPrincipalName() {
		if(principal==null) {
			return StringUtils.defaultString(lastPrincipalName);
		} else {
			return principal.getPrincipalName();
		}
	}
	
	public String getLastRealmName() {
		if(realm==null) {
			if(lastRealmName==null) {
				return "";
			} else {
				return lastRealmName;
			}
		} else {
			return realm.getName();
		}
	}

	public String getUserAgent() {
		return environment.get(BrowserEnvironment.USER_AGENT.toString()).toString();
	}
	
	public void setEnvironmentVariable(String key, Object value) {
		environment.put(key, value);
	}

	public Object removeEnvironmentVariable(String name) {
		return environment.remove(name);		
	}
	
	public boolean hasEnvironmentVariable(String key) {
		return environment.containsKey(key);
	}
	
	public Object getEnvironmentVariable(String key){ 
		return environment.get(key);
	}

	public void fakeCredentials() {
		setPrincipal(new FakePrincipal(lastPrincipalName));
		Realm realm = new Realm();
		realm.setId(-1L);
		realm.setName("Fake");
	}
	
	public boolean hasNextStep() {
		return currentIndex < modules.size() - 1;
	}

	public void authAttempted() {
		attempts++;
	}

	public int getAttempts() {
		return attempts;
	}
	
	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}
	
	public String getHomePage() {
		return homePage;
	}

	public Map<String,String> getParameters() {
		return parameters;
	}

	public Map<String,String[]> getRequestParameters() {
		return requestParameters;
	}
	
	public boolean isHTTP() {
		return environment.containsKey(BrowserEnvironment.HOST.toString());
	}

	public Principal getLastPrincipal() {
		return lastPrincipal;
	}

	public void setLastPrincipal(Principal lastPrincipal) {
		this.lastPrincipal = lastPrincipal;
	}

	public Realm getHostRealm() {
		return hostRealm;
	}

	public void setHostRealm(Realm hostRealm) {
		this.hostRealm = hostRealm;
	}

	public boolean hasEnded() {
		return hasEnded;
	}

	public void setHasEnded(boolean hasEnded) {
		this.hasEnded = hasEnded;
	}
	
	public static AuthenticationState getCurrentState(HttpServletRequest request) {
		
		return (AuthenticationState)
				request.getSession().getAttribute(AUTHENTICATION_STATE);
	
	}
	
	public static AuthenticationState getOrCreateState(String logonScheme, 
			HttpServletRequest request, 

			Realm realm, 
			AuthenticationState mergeState,
			Locale locale) throws AccessDeniedException {
		
		AuthenticationState state = getCurrentState(request);
		
		if(!Objects.isNull(state)) {
			return state;
		}
		
		return createAuthenticationState(logonScheme,
					request, realm, mergeState, locale);
	}
	public static AuthenticationState createAuthenticationState(String logonScheme, 
			HttpServletRequest request, 
			Realm realm, 
			AuthenticationState mergeState,
			Locale locale) throws AccessDeniedException {
		return createAuthenticationState(logonScheme, request, realm, null, mergeState, locale);
	}
	
	public static AuthenticationState createAuthenticationState(String logonScheme, 
			HttpServletRequest request, 
			Realm realm, 
			Principal principal,
			AuthenticationState mergeState,
			Locale locale) throws AccessDeniedException {
		
		AuthenticationService authenticationService = ApplicationContextServiceImpl.getInstance().getBean(AuthenticationService.class);
		RealmService realmService = ApplicationContextServiceImpl.getInstance().getBean(RealmService.class);
		
		
		if(realm==null) {
			realm = realmService.getRealmByHost(request.getServerName());
		}

		Map<String, Object> environment = new HashMap<String, Object>();
		for (BrowserEnvironment env : BrowserEnvironment.values()) {
			if (request.getHeader(env.toString()) != null) {
				environment.put(env.toString(),
						request.getHeader(env.toString()));
			}
		}
		Map<String, Cookie> cookies = new HashMap<>();
		for(Cookie cookie : request.getCookies()) {
			cookies.put(cookie.getName(), cookie);
		}
		environment.put(COOKIES, cookies);
		
		String originalUri = (String)request.getAttribute("browserRequestUri");
		if(originalUri!=null) {
			environment.put("originalUri", originalUri);
		}
		environment.put("uri", request.getRequestURI());
		environment.put("url", request.getRequestURL().toString());

		AuthenticationState state = authenticationService
				.createAuthenticationState(logonScheme, 
						request.getRemoteAddr(), 
						environment, realm, locale);
		
		state.getRequestParameters().putAll(request.getParameterMap());
		
		List<AuthenticationModule> modules = state.getModules();
		for(AuthenticationModule module : modules) {
			if(authenticationService.getAuthenticator(module.getTemplate())==null) {
				
				state = createAuthenticationState("fallback", request, realm, mergeState, locale);
				state.setLastErrorIsResourceKey(true);
				state.setLastErrorMsg("revertedFallback.adminOnly");
				return state;
			}
		}
		
		if(Objects.nonNull(principal)) {
			state.setPrincipal(principal);
		}
		
		if(mergeState!=null) {
			state.getParameters().putAll(mergeState.getParameters());
			state.setLastErrorIsResourceKey(mergeState.getLastErrorIsResourceKey());
			state.setLastErrorMsg(mergeState.getLastErrorMsg());
		} else {
			Enumeration<?> names = request.getParameterNames();
			while(names.hasMoreElements()) {
				String name = (String) names.nextElement();
				state.addParameter(name, request.getParameter(name));
			}
		}

		request.getSession().setAttribute(AUTHENTICATION_STATE, state);
	
		return state;
	}

	public static void clearCurrentState(HttpServletRequest request) {
		
		AuthenticationState currentState = AuthenticationState.getCurrentState(request);
		request.getSession().setAttribute(AUTHENTICATION_STATE, null);
		
		if(currentState!=null) {
			request.getSession().setAttribute(PREVIOUS_AUTHENTICATION_SCHEME,
					currentState.getScheme().getResourceKey());
		}
		
		
		
	}

	public boolean containsModule(String... resourceKeys) {
		
		Set<String> tmp = new HashSet<>(Arrays.asList(resourceKeys));
		for(AuthenticationModule module : modules) {
			if(tmp.contains(module.getTemplate())) {
				return true;
			}
		}
		return false;
	}

	public static AuthenticationState createAuthenticationState(String scheme, HttpServletRequest request,
			Realm currentRealm) throws AccessDeniedException {
		return createAuthenticationState(scheme, request, currentRealm, null, null);
	}
}
