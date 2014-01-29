package com.hypersocket.config;

import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ConfigurationEvent extends SessionEvent {

	private static final long serialVersionUID = 5849555055879289458L;

	public static final String ATTR_CONFIG_DISPLAY_NAME = "attr.configItem";
	public static final String ATTR_CONFIG_RESOURCE_KEY = "attr.configResourceKey";
	public static final String ATTR_OLD_VALUE = "attr.configOldValue";
	public static final String ATTR_NEW_VALUE = "attr.configNewValue";
	
	public ConfigurationEvent(Object source, String resourceKey,
			boolean success, Session session, PropertyTemplate property, String oldValue, String newValue) {
		super(source, resourceKey, success, session);
		addAttribute(ATTR_CONFIG_DISPLAY_NAME, I18NServiceImpl.tagForConversion(
				property.getCategory().getBundle(), 
				property.getResourceKey()));
		addAttribute(ATTR_CONFIG_RESOURCE_KEY, property.getResourceKey());
		addAttribute(ATTR_OLD_VALUE, oldValue);
		addAttribute(ATTR_NEW_VALUE, newValue);
	}

	public ConfigurationEvent(Object source, String resourceKey, Throwable e,
			Session session) {
		super(source, resourceKey, e, session);
	}

}
