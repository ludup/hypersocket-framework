package com.hypersocket.automation.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.automation.AutomationResourceServiceImpl;
import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;

public class AutomationTaskFinishedEvent extends
		SystemEvent {

	private static final long serialVersionUID = -3063880990945502517L;

	public static final String EVENT_RESOURCE_KEY = "automation.finished";
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	public AutomationTaskFinishedEvent(Object source,
			AutomationResource resource) {
		super(source, EVENT_RESOURCE_KEY, true, resource.getRealm());
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	public AutomationTaskFinishedEvent(Object source,
			AutomationResource resource, Throwable e) {
		super(source, EVENT_RESOURCE_KEY, e, resource.getRealm());
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
	}

	@Override
	public String getResourceBundle() {
		return AutomationResourceServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
