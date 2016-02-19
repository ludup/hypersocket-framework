package com.hypersocket.triggers;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.Task;

public abstract class AbstractTaskResult extends SystemEvent implements TaskResult {

	private static final long serialVersionUID = 5664474659342093254L;

	public static final String EVENT_RESOURCE_KEY = "task.event";
	
	public static final String ATTR_TASK_NAME = "attr.taskName";
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	
	public AbstractTaskResult(Object source, String resourceKey, boolean success,
			Realm currentRealm, Task task) {
		super(source, resourceKey, success, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
		addAttribute(ATTR_REALM_NAME, currentRealm.getName());
	}

	public AbstractTaskResult(Object source, String resourceKey,
			SystemEventStatus status, Realm currentRealm, Task task) {
		super(source, resourceKey, status, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
		addAttribute(ATTR_REALM_NAME, currentRealm.getName());
	}

	public AbstractTaskResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, Task task) {
		super(source, resourceKey, e, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
		addAttribute(ATTR_REALM_NAME, currentRealm.getName());
	}

	public abstract boolean isPublishable();

	@Override
	public abstract String getResourceBundle();

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public SystemEvent getEvent() {
		return this;
	}
}
