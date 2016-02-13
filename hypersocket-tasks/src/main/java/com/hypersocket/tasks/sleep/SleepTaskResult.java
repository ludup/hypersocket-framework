package com.hypersocket.tasks.sleep;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class SleepTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = -7162238341955578989L;

	public static final String EVENT_RESOURCE_KEY = "sleep.result";
	
	public static final String ATTR_SLEEP_TIME = "attr.sleepTime";
	
	public SleepTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, Long sleepTime) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_SLEEP_TIME, sleepTime.toString());
	}

	public SleepTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task, Long sleepTime) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_SLEEP_TIME, sleepTime.toString());
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return SleepTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
