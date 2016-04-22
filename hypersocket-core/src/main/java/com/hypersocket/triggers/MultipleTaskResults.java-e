package com.hypersocket.triggers;

import java.util.Collection;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;

public class MultipleTaskResults extends AbstractTaskResult {

	private static final long serialVersionUID = -1287105067476106327L;

	TaskResult[] results;
	public MultipleTaskResults(Object source, Realm currentRealm, Task task, TaskResult... results) {
		super(source, "multiple.results", true, currentRealm, task);
		this.results = results;
	}
	
	public MultipleTaskResults(Object source, Realm currentRealm, Task task, Collection<TaskResult> results) {
		super(source, "multiple.results", true, currentRealm, task);
		this.results = results.toArray(new TaskResult[0]);
	}

	@Override
	public boolean isPublishable() {
		return false;
	}

	public TaskResult[] getResults() {
		return results;
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}
}
