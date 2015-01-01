package com.hypersocket.triggers;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;

public abstract class TaskResult extends SystemEvent {

	private static final long serialVersionUID = 5664474659342093254L;

	public static final String ATTR_TASK_NAME = "attr.taskName";
	
	public TaskResult(Object source, String resourceKey, boolean success,
			Realm currentRealm, Task task) {
		super(source, resourceKey, success, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
	}

	public TaskResult(Object source, String resourceKey,
			SystemEventStatus status, Realm currentRealm, Task task) {
		super(source, resourceKey, status, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
	}

	public TaskResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, Task task) {
		super(source, resourceKey, e, currentRealm);
		addAttribute(ATTR_TASK_NAME, task.getName());
	}

	public abstract boolean isPublishable();

	@Override
	public abstract String getResourceBundle();

}
