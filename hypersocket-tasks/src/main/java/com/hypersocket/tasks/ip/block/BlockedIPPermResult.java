package com.hypersocket.tasks.ip.block;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;

public class BlockedIPPermResult extends BlockedIPResult {

	private static final long serialVersionUID = -517243631983981802L;

	public static final String EVENT_RESOURCE_KEY = "blocked.perm.ip";
	
	public BlockedIPPermResult(Object source, Realm currentRealm, Task task,
			String ipAddress) {
		super(source, EVENT_RESOURCE_KEY, currentRealm, task, ipAddress);
	}

	public BlockedIPPermResult(Object source, boolean failed,
			Realm currentRealm, Task task, String ipAddress) {
		super(source, EVENT_RESOURCE_KEY, failed, currentRealm, task, ipAddress);
	}

	public BlockedIPPermResult(Object source, Throwable e, Realm currentRealm,
			Task task, String ipAddress) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task, ipAddress);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
