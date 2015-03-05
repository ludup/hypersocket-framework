package com.hypersocket.triggers.actions.ip;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class BlockedIPResult extends TaskResult {

	private static final long serialVersionUID = 1931288302204904429L;

	public static final String EVENT_RESOURCE_KEY = "blocked.ip";
	
	public static final String ATTR_BLOCKED_IP = "attr.blockedIp";
	public static final String ATTR_BLOCK_LENGTH = "attr.blockLength";
	
	
	public BlockedIPResult(Object source, Realm currentRealm, Task task, String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
	}
	
	public BlockedIPResult(Object source, boolean failed, Realm currentRealm, Task task, String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, failed ? SystemEventStatus.FAILURE : SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
	}
	
	public BlockedIPResult(Object source, Throwable e,
			Realm currentRealm, Task task, String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
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
