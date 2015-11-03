package com.hypersocket.tasks.ip.block;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;

public class BlockedIPTempResult extends BlockedIPResult {

	private static final long serialVersionUID = 231202610338051806L;

	public static final String EVENT_RESOURCE_KEY = "blocked.temp.ip";
	public static final String ATTR_BLOCK_LENGTH = "attr.blockLength";
	
	public BlockedIPTempResult(Object source, Realm currentRealm, Task task,
			String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, currentRealm, task, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
	}

	public BlockedIPTempResult(Object source, boolean failed,
			Realm currentRealm, Task task, String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, failed, currentRealm, task, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
	}

	public BlockedIPTempResult(Object source, Throwable e, Realm currentRealm,
			Task task, String ipAddress, int length) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task, ipAddress);
		addAttribute(ATTR_BLOCK_LENGTH, String.valueOf(length));
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
