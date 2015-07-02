package com.hypersocket.attributes.user;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.AuthenticatorResult;
import com.hypersocket.auth.PostAuthenticationStep;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.RealmService;

@Component
public class LoadProfilePostAuthenticationStep implements
		PostAuthenticationStep {

	static Logger log = LoggerFactory.getLogger(LoadProfilePostAuthenticationStep.class);
	@Autowired
	RealmService realmService; 
	
	@Autowired
	AuthenticationService authenticationService; 
	
	public LoadProfilePostAuthenticationStep() {
	}

	@PostConstruct
	private void postConstruct() {
		authenticationService.registerPostAuthenticationStep(this);
	}
	
	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		return state.getPrincipal()!=null;
	}

	@Override
	public int getOrderPriority() {
		return 0;
	}

	@Override
	public String getResourceKey() {
		return "loadProfile";
	}

	@Override
	public AuthenticatorResult process(final AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters)
			throws AccessDeniedException {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Loading " + state.getPrincipal().getPrincipalName() + " profile");
			}
			realmService.getUserPropertyTemplates(state.getPrincipal());
			if(log.isInfoEnabled()) {
				log.info("Loaded " + state.getPrincipal().getPrincipalName() + " profile");
			}
		} catch (AccessDeniedException e) {
		}
		return AuthenticatorResult.AUTHENTICATION_SUCCESS;
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state) {
		return null;
	}

	@Override
	public boolean requiresUserInput(AuthenticationState state) {
		return false;
	}

	@Override
	public boolean requiresSession(AuthenticationState state) {
		return true;
	}

}
