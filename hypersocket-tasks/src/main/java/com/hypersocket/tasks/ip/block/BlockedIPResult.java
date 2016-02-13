package com.hypersocket.tasks.ip.block;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class BlockedIPResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1931288302204904429L;

	public static final String EVENT_RESOURCE_KEY = "blocked.ip";
	public static final String ATTR_BLOCKED_IP = "attr.blockedIp";
	
	public BlockedIPResult(Object source, String resourceKey, Realm currentRealm, Task task, String ipAddress) {
		super(source, resourceKey, SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
	}
	
	public BlockedIPResult(Object source, String resourceKey, boolean failed, Realm currentRealm, Task task, String ipAddress) {
		super(source, resourceKey, failed ? SystemEventStatus.FAILURE : SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
	}
	
	public BlockedIPResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, Task task, String ipAddress) {
		super(source, resourceKey, e, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
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
