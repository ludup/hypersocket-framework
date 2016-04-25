package com.hypersocket.tasks.suspend;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class SuspendUserResult extends AbstractTaskResult {

	private static final long serialVersionUID = 8659070892407924656L;

	public static final String EVENT_RESOURCE_KEY = "suspendUser.result";
	public static final String ATTR_SUSPENDED_USER = "suspendUser.name";
	public static final String ATTR_START_DATE = "suspendUser.startDate";
	public static final String ATTR_DURATION = "suspendUser.duration";
	
	public SuspendUserResult(Object source, Realm currentRealm, Task task,
			String name, Date startDate, Long duration) {
		super(source, EVENT_RESOURCE_KEY, SystemEventStatus.SUCCESS, currentRealm,
				task);
		addAttribute(ATTR_SUSPENDED_USER, name);
		addAttribute(ATTR_START_DATE, startDate);
		addAttribute(ATTR_DURATION, duration);
	}

	public SuspendUserResult(Object source, Throwable e, Realm currentRealm,
			Task task, String name, Date startDate, Long duration) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_SUSPENDED_USER, name);
		addAttribute(ATTR_START_DATE, startDate);
		addAttribute(ATTR_DURATION, duration);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return SuspendUserTask.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
