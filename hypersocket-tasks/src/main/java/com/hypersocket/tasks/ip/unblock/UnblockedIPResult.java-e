package com.hypersocket.tasks.ip.unblock;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.ip.block.BlockIPTask;
import com.hypersocket.triggers.AbstractTaskResult;

public class UnblockedIPResult extends AbstractTaskResult {

	private static final long serialVersionUID = 5303654508133707273L;
	
	public static final String EVENT_RESOURCE_KEY = "unblocked.ip";
	
	public static final String ATTR_BLOCKED_IP = "attr.blockedIp";
	
	public UnblockedIPResult(Object source, Realm currentRealm, Task task, String ipAddress) {
		super(source, EVENT_RESOURCE_KEY, SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_BLOCKED_IP, ipAddress);
	}

	
	public UnblockedIPResult(Object source, Throwable e,
			Realm currentRealm, Task task, String ipAddress) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
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
