package com.hypersocket.tasks.ip.monitor;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class MonitorPortResult extends AbstractTaskResult {

	private static final long serialVersionUID = 763159905635810105L;

	public static final String EVENT_RESOURCE_KEY = "monitorPort.result";
	
	public static final String ATTR_IP = "attr.host";
	public static final String ATTR_PORT = "attr.port";
	public static final String ATTR_TIMEOUT = "attr.timeout";
	
	
	public MonitorPortResult(Object source, boolean success,
			Realm currentRealm, Task task, String ip, long port, long timeout) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm,
				task);
		addAttribute(ATTR_IP, ip);
		addAttribute(ATTR_PORT, port);
		addAttribute(ATTR_TIMEOUT, timeout);
	}

	public MonitorPortResult(Object source, Throwable e, Realm currentRealm,
			Task task, String ip, long port, long timeout) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_IP, ip);
		addAttribute(ATTR_PORT, port);
		addAttribute(ATTR_TIMEOUT, timeout);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return MonitorPortTask.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
