package com.hypersocket.search.task;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class SearchResourceTaskResult extends TaskResult {

	private static final long serialVersionUID = -3419349802806414943L;

	public static final String EVENT_RESOURCE_KEY = "searchResource.result";
	
	public SearchResourceTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
	}

	public SearchResourceTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return SearchResourceTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
