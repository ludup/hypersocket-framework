package com.hypersocket.automation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.automation.AutomationResourceServiceImpl;
import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;

public class AutomationTaskStartedEvent extends
		SystemEvent {

	private static final long serialVersionUID = -3063880990945502517L;

	public static final String EVENT_RESOURCE_KEY = "automation.started";
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	public AutomationTaskStartedEvent(Object source,
			AutomationResource resource) {
		super(source, EVENT_RESOURCE_KEY, true, resource.getRealm());
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		addAttribute(ATTR_REALM_NAME, resource.getRealm().getName());
	}

	public AutomationTaskStartedEvent(Object source,
			Realm realm, Throwable e) {
		super(source, EVENT_RESOURCE_KEY, e, realm);
		addAttribute(ATTR_REALM_NAME, realm.getName());
	}

	@Override
	public String getResourceBundle() {
		return AutomationResourceServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
