package com.hypersocket.authenticator.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.session.Session;

public class ModulePropertyChangedEvent extends ModulePropertyEvent {

	private static final long serialVersionUID = -6529301143545377461L;

	public static final String EVENT_RESOURCE_KEY = "property.changed";

	public ModulePropertyChangedEvent(Object source, boolean success,
			Session session, AuthenticationScheme resource, String module,
			String property, String propertyValue, String oldPropertyValue) {
		super(source, success, session, resource, module,
				property, propertyValue, oldPropertyValue);
	}

	public ModulePropertyChangedEvent(Object source, Session session,
			Throwable e, AuthenticationScheme resource, String module,
			String property, String propertyValue, String oldPropertyValue) {
		super(source, session, e, resource, module, property,
				propertyValue, oldPropertyValue);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
