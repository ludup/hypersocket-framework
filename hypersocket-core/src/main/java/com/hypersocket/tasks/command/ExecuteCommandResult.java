package com.hypersocket.tasks.command;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class ExecuteCommandResult extends TaskResult {

	public ExecuteCommandResult(Object source, String resourceKey,
			boolean success, Realm currentRealm, Task task) {
		super(source, resourceKey, success, currentRealm, task);
		// TODO Auto-generated constructor stub
	}

	public ExecuteCommandResult(Object source, String resourceKey,
			SystemEventStatus status, Realm currentRealm, Task task) {
		super(source, resourceKey, status, currentRealm, task);
		// TODO Auto-generated constructor stub
	}

	public ExecuteCommandResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, Task task) {
		super(source, resourceKey, e, currentRealm, task);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isPublishable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getResourceBundle() {
		// TODO Auto-generated method stub
		return null;
	}

}
