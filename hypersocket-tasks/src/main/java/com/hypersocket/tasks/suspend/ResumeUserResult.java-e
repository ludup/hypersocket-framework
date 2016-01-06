package com.hypersocket.tasks.suspend;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.ip.block.BlockIPTask;
import com.hypersocket.triggers.AbstractTaskResult;

public class ResumeUserResult extends AbstractTaskResult {

	private static final long serialVersionUID = -4411122520620089664L;
	
	public static final String EVENT_RESOURCE_KEY = "resumeUser.result";
	
	public static final String ATTR_RESUMED_USER = "resumeUser.name";
	
	public ResumeUserResult(Object source, Realm currentRealm, Task task,
			String name) {
		super(source, EVENT_RESOURCE_KEY, SystemEventStatus.SUCCESS,
				currentRealm, task);
		addAttribute(ATTR_RESUMED_USER, name);
	}

	public ResumeUserResult(Object source, Throwable e, Realm currentRealm,
			Task task, String name) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_RESUMED_USER, name);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return BlockIPTask.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
