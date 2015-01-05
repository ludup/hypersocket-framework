package com.hypersocket.triggers.actions.ip;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class BlockedIPResult extends TaskResult {

	private static final long serialVersionUID = 1931288302204904429L;

	public BlockedIPResult(Object source, Realm currentRealm, Task task, String ipAddress) {
		super(source, "blocked.ip", SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute("block.ip", ipAddress);
	}

	
	public BlockedIPResult(Object source, Throwable e,
			Realm currentRealm, Task task, String ipAddress) {
		super(source, "blocked.ip", e, currentRealm, task);
		addAttribute("block.ip", ipAddress);
	}


	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return BlockIPTask.RESOURCE_BUNDLE;
	}

}
