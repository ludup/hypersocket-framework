package com.hypersocket.config;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class ConfigurationValueChangedEvent extends SessionEvent {

	private static final long serialVersionUID = 5849555055879289458L;

	public static final String EVENT_RESOURCE_KEY = "config.changed";

	public static final String ATTR_CONFIG_DISPLAY_NAME = "attr.configItem";
	public static final String ATTR_CONFIG_RESOURCE_KEY = "attr.configResourceKey";
	public static final String ATTR_OLD_VALUE = "attr.configOldValue";
	public static final String ATTR_NEW_VALUE = "attr.configNewValue";

	public ConfigurationValueChangedEvent(Object source, boolean success,
			Session session, PropertyTemplate property, String oldValue,
			String newValue, boolean hidden, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, success, session, realm, hidden);
		addAttribute(ATTR_CONFIG_DISPLAY_NAME,
				I18NServiceImpl.tagForConversion(property.getCategory()
						.getBundle(), property.getResourceKey()));
		addAttribute(ATTR_CONFIG_RESOURCE_KEY, property.getResourceKey());
		String[] oldValues = ResourceUtils.explodeValues(oldValue);
		if(oldValues.length==1) {
			addAttribute(ATTR_OLD_VALUE, oldValue);
		} else {
			addAttribute(ATTR_OLD_VALUE, StringUtils.arrayToDelimitedString(oldValues, "\r\n"));
		}
		String[] newValues = ResourceUtils.explodeValues(newValue);
		if(newValues.length==1) {
			addAttribute(ATTR_NEW_VALUE, newValue);
		} else {
			addAttribute(ATTR_NEW_VALUE, StringUtils.arrayToDelimitedString(newValues, "\r\n"));
		}
		
	}

	public ConfigurationValueChangedEvent(Object source, String resourceKey,
			Throwable e, Session session, Realm realm) {
		super(source, resourceKey, e, session, realm);
	}
	
	public String getConfigResourceKey() {
		return org.apache.commons.lang3.StringUtils.defaultString(getAttribute(ATTR_CONFIG_RESOURCE_KEY), "");
	}
	
	public String getOldValue() {
		return getAttribute(ATTR_OLD_VALUE);
	}
	
	public String getNewValue() {
		return getAttribute(ATTR_NEW_VALUE);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
