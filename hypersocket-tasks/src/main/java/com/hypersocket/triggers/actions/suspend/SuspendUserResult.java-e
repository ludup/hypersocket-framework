package com.hypersocket.triggers.actions.suspend;

import java.util.Date;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class SuspendUserResult extends TaskResult {

	private static final long serialVersionUID = 8659070892407924656L;

	public SuspendUserResult(Object source, Realm currentRealm, Task task,
			String name, Date startDate, Long duration) {
		super(source, "suspendUser.name", SystemEventStatus.SUCCESS, currentRealm,
				task);
		addAttribute("suspendUser.name", name);
		addAttribute("suspendUser.startDate", startDate);
		addAttribute("suspendUser.duration", duration);
	}

	public SuspendUserResult(Object source, Throwable e, Realm currentRealm,
			Task task, String name, Date startDate, Long duration) {
		super(source, "blocked.ip", e, currentRealm, task);
		addAttribute("suspendUser.name", name);
		addAttribute("suspendUser.startDate", startDate);
		addAttribute("suspendUser.duration", duration);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return SuspendUserTask.RESOURCE_BUNDLE;
	}

}
