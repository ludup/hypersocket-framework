package com.hypersocket.authenticator.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.profile.ProfileBatchChangeEvent;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class AuthenticationSchemeEvent extends ResourceEvent implements ProfileBatchChangeEvent {

	private static final long serialVersionUID = 6942550615160069826L;
	
	public static final String EVENT_RESOURCE_KEY = "authenticationScheme.event";
	
	public static final String ATTR_AUTHENTICATION_SCHEME = "attr.authenticationSchemeName";
	public static final String ATTR_AUTHENTICATION_SCHEME_MODULES = "attr.authenticationSchemeModules";
	public static final String ATTR_AUTHENTICATION_OLD_SCHEME_MODULES = "attr.authenticationOldSchemeModules";

	public AuthenticationSchemeEvent(Object source, String resourceKey,
			boolean success, Session session, AuthenticationScheme resource,
			String[] moduleList, String[] oldModuleList) {
		super(source, resourceKey, success, session, resource);
		addAuthenticationSchemeAttribute(resource, moduleList, oldModuleList);
	}

	public AuthenticationSchemeEvent(Object source, String resourceKey,
			Session session, Throwable e, AuthenticationScheme resource,
			String[] moduleList, String[] oldModuleList) {
		super(source, resourceKey, e, session, resource);
		addAuthenticationSchemeAttribute(resource, moduleList, oldModuleList);
	}

	private void addAuthenticationSchemeAttribute(
			AuthenticationScheme resource, String[] moduleList, String[] oldModuleList) {
		addAttribute(ATTR_AUTHENTICATION_SCHEME, resource.getName());
		List<String> moduleNames = new ArrayList<String>();
		List<String> oldModuleNames = new ArrayList<String>();
		
		if(Objects.nonNull(moduleList)) {
			for(String module : moduleList) {
				moduleNames.add(module);
			}
			
			addAttribute(ATTR_AUTHENTICATION_SCHEME_MODULES,
					StringUtils.collectionToCommaDelimitedString(moduleNames));
		}
		if(Objects.nonNull(oldModuleList)) {
		
			for(String module : oldModuleList) {
				oldModuleNames.add(module);
			}
			addAttribute(ATTR_AUTHENTICATION_OLD_SCHEME_MODULES,
				StringUtils.collectionToCommaDelimitedString(oldModuleNames));
		}
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
