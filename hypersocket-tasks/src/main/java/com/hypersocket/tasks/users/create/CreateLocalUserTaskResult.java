package com.hypersocket.tasks.users.create;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class CreateLocalUserTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "createLocalUser.result";
	
	public CreateLocalUserTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
	}

	public CreateLocalUserTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return CreateLocalUserTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
