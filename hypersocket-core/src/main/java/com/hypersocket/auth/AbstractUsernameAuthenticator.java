package com.hypersocket.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.util.ArrayValueHashMap;


public abstract class AbstractUsernameAuthenticator implements Authenticator {

	@Autowired
	private RealmService realmService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private RealmRepository realmRepository;
	
	@Autowired
	private ConfigurationService configurationService; 
	
	@Override
	public AuthenticatorResult authenticate(AuthenticationState state,
			Map<String, String[]> parameters)
			throws AccessDeniedException {

		String username = ArrayValueHashMap.getSingle(parameters,
				UsernameAndPasswordTemplate.USERNAME_FIELD);

		if (username == null || username.equals("")) {
			username = state.getParameter(UsernameAndPasswordTemplate.USERNAME_FIELD);
		}

		if (Objects.isNull(state.getPrincipal())  && (username == null || username.equals(""))) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}
		
		Realm selectedRealm = null;
		if(parameters.containsKey("realm")) {
			String name =  ArrayValueHashMap.getSingle(parameters, "realm");
			if(systemConfigurationService.getBooleanValue("auth.enforceSelectRealm")) {
				if(StringUtils.isBlank(name)) {
					state.setLastErrorIsResourceKey(true);
					state.setLastErrorMsg("error.userMustSelectRealm");
					return AuthenticatorResult.AUTHENTICATION_FAILURE_DISPLAY_ERROR;
				}
			}
			selectedRealm = realmService.getRealmByName(name);
		}

		if(!processFields(state, parameters)) {
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}
		
		try {
			Principal principal;
			
			if(Objects.isNull(state.getPrincipal())) {
				principal = authenticationService.resolvePrincipalAndRealm(
					state, username, selectedRealm, state.getPrincipalType());
			} else {
				principal = state.getPrincipal();
			}

			AuthenticatorResult result = verifyCredentials(state, principal, parameters);

			if (result == AuthenticatorResult.AUTHENTICATION_SUCCESS) {
				state.setRealm(principal.getRealm());
				state.setPrincipal(principal);
			}

			return result;
		} catch (PrincipalNotFoundException e) {
			if(configurationService.getBooleanValue(state.getRealm()==null? realmService.getDefaultRealm() : state.getRealm(), "logon.verboseErrors")) {
				state.setLastErrorIsResourceKey(false);
				state.setLastErrorMsg(e.getMessage());
				return AuthenticatorResult.AUTHENTICATION_FAILURE_DISPLAY_ERROR;
			}
			return AuthenticatorResult.AUTHENTICATION_FAILURE_INVALID_PRINCIPAL;
		}

	}
	
	protected List<Realm> getLogonRealms(AuthenticationState state) {
		List<Realm> realms = new ArrayList<Realm>();
		if(state.getHostRealm()==null) {
			if (systemConfigurationService.getBooleanValue("auth.chooseRealm")) {
				if(!realmService.isRealmStrictedToHost(state.getRealm())) {		
					for(Realm realm : realmRepository.allRealms()) {
						if(!realmService.isRealmStrictedToHost(realm)) {
							realms.add(realm);
						}
					}
				}
			}
		}
//		}
		return realms;
	}
	
	protected abstract boolean processFields(AuthenticationState state,
			Map<String, String[]> parameters);

	protected abstract AuthenticatorResult verifyCredentials(AuthenticationState state,
			Principal principal, 
			Map<String, String[]> parameters);
	
	public boolean isHidden() {
		return false;
	}
	
	@Override
	public boolean canAuthenticate(Principal principal) throws AccessDeniedException {
		return true;
	}

}
