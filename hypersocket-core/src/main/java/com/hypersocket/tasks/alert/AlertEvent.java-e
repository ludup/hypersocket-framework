package com.hypersocket.tasks.alert;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class AlertEvent extends AbstractTaskResult {

	private static final long serialVersionUID = -8241348099506840665L;

	public static final String EVENT_RESOURCE_KEY = "event.alert";
	
	public static final String ATTR_THRESHOLD = "attr.threshold";
	public static final String ATTR_TIMEOUT = "attr.timeout";
	public static final String ATTR_TRIGGER_NAME = "attr.triggerName";
	public static final String ATTR_TASK_NAME = "attr.taskName";
	
	public AlertEvent(Object source, String resourceKey, boolean success,
			Realm currentRealm, int threshold, int timeout,  Task task, SystemEvent alertEvent) {
		super(source, resourceKey + "." + task.getId(), SystemEventStatus.WARNING, currentRealm, task);
		addAttribute(ATTR_THRESHOLD, threshold);
		addAttribute(ATTR_TIMEOUT, timeout);
		addAttribute(ATTR_TASK_NAME, task.getName());
		
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
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), getResourceKey());
	}

}
