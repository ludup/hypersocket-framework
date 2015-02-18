package com.hypersocket.triggers.actions.suspend;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.actions.ip.BlockIPTask;

public class ResumeUserResult extends TaskResult {

	private static final long serialVersionUID = -4411122520620089664L;

	public ResumeUserResult(Object source, Realm currentRealm, Task task,
			String name) {
		super(source, "resumeUser.name", SystemEventStatus.SUCCESS,
				currentRealm, task);
		addAttribute("resumeUser.name", name);
	}

	public ResumeUserResult(Object source, Throwable e, Realm currentRealm,
			Task task, String ipAddress) {
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
