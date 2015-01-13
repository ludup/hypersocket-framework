package com.hypersocket.triggers.actions.ip;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class MonitorPortResult extends TaskResult {

	private static final long serialVersionUID = 763159905635810105L;

	public MonitorPortResult(Object source, boolean success,
			Realm currentRealm, Task task, String ip, long port, long timeout) {
		super(source, MonitorPortTask.TASK_RESOURCE_KEY, success, currentRealm,
				task);
		addAttribute(MonitorPortTask.ATTR_IP, ip);
		addAttribute(MonitorPortTask.ATTR_PORT, port);
		addAttribute(MonitorPortTask.ATTR_TIMEOUT, timeout);
	}

	public MonitorPortResult(Object source, Throwable e, Realm currentRealm,
			Task task, String ip, long port, long timeout) {
		super(source, MonitorPortTask.TASK_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(MonitorPortTask.ATTR_IP, ip);
		addAttribute(MonitorPortTask.ATTR_PORT, port);
		addAttribute(MonitorPortTask.ATTR_TIMEOUT, timeout);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return MonitorPortTask.RESOURCE_BUNDLE;
	}

}
