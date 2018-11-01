package com.hypersocket.authenticator.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ModulePropertyEvent extends SessionEvent {

	private static final long serialVersionUID = 3941828114867292915L;

	public static final String EVENT_RESOURCE_KEY = "modulePropertyChanged.event";

	public static final String ATTR_AUTHENTICATION_SCHEME = "attr.authenticationSchemeName";
	public static final String ATTR_AUTHENTICATION_SCHEME_MODULE = "attr.authenticationSchemeModule";
	public static final String ATTR_AUTHENTICATION_PROPERTY_NAME = "attr.authenticationPropertyName";
	public static final String ATTR_NEW_PROPERTY_VALUE = "attr.newPropertyValue";
	public static final String ATTR_OLD_PROPERTY_VALUE = "attr.oldPropertyValue";

	public ModulePropertyEvent(Object source, boolean success, Session session,
			AuthenticationScheme resource, String module, String property,
			String propertyValue, String oldPropertyValue) {
		super(resource, EVENT_RESOURCE_KEY, success, session);
		addAuthenticationSchemeAttribute(resource, module, property,
				propertyValue, oldPropertyValue);
	}

	public ModulePropertyEvent(Object source, Session session, Throwable e,
			AuthenticationScheme resource, String module, String property,
			String propertyValue, String oldPropertyValue) {
		super(resource, EVENT_RESOURCE_KEY, e, session);
		addAuthenticationSchemeAttribute(resource, module, property,
				propertyValue, oldPropertyValue);
	}

	private void addAuthenticationSchemeAttribute(
			AuthenticationScheme resource, String module, String property,
			String propertyValue, String oldPropertyValue) {
		addAttribute(ATTR_AUTHENTICATION_SCHEME, resource.getName());
		addAttribute(ATTR_AUTHENTICATION_SCHEME_MODULE, module);
		addAttribute(ATTR_AUTHENTICATION_PROPERTY_NAME, property);
		addAttribute(ATTR_NEW_PROPERTY_VALUE, propertyValue);
		addAttribute(ATTR_OLD_PROPERTY_VALUE, oldPropertyValue);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
