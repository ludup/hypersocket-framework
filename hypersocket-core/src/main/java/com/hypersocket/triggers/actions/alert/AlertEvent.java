package com.hypersocket.triggers.actions.alert;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class AlertEvent extends ActionResult {

	private static final long serialVersionUID = -8241348099506840665L;

	public static final String EVENT_RESOURCE_KEY = "event.alert";
	
	public static final String ATTR_THRESHOLD = "attr.threshold";
	public static final String ATTR_TIMEOUT = "attr.timeout";
	public static final String ATTR_TRIGGER_NAME = "attr.triggerName";
	public static final String ATTR_ACTION_NAME = "attr.actionName";
	
	public AlertEvent(Object source, String resourceKey, boolean success,
			Realm currentRealm, int threshold, int timeout, TriggerAction action, SystemEvent alertEvent) {
		super(source, resourceKey + "." + action.getId(), SystemEventStatus.WARNING, currentRealm, action);
		addAttribute(ATTR_THRESHOLD, threshold);
		addAttribute(ATTR_TIMEOUT, timeout);
		addAttribute(ATTR_ACTION_NAME, action.getName());
		addAttribute(ATTR_TRIGGER_NAME, action.getTrigger().getName());
		
		addAllAttributes(alertEvent.getAttributes());
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

}
