package com.hypersocket.tasks.digest;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class DigestTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "digest.result";
	
	public static final String ATTR_HASH_OUTPUT = "attr.hashOutput";
	
	public DigestTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, String hashOutput) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_HASH_OUTPUT, hashOutput);
	}

	public DigestTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return DigestTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
