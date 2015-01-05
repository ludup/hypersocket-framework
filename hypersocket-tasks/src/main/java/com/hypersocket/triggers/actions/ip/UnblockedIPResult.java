package com.hypersocket.triggers.actions.ip;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class UnblockedIPResult extends TaskResult {

	private static final long serialVersionUID = 5303654508133707273L;

	public UnblockedIPResult(Object source, Realm currentRealm, Task task, String ipAddress) {
		super(source, "unblocked.ip", SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute("unblock.ip", ipAddress);
	}

	
	public UnblockedIPResult(Object source, Throwable e,
			Realm currentRealm, Task task, String ipAddress) {
		super(source, "unblocked.ip", e, currentRealm, task);
		addAttribute("unblock.ip", ipAddress);
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
