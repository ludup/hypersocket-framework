package com.hypersocket.tasks.principal.legacy;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class ImportPrincipalLegacyIDTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "importPrincipalLegacyID.result";
	
	public ImportPrincipalLegacyIDTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
	}

	public ImportPrincipalLegacyIDTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return ImportPrincipalLegacyIDTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
