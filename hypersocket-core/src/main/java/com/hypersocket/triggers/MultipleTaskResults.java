package com.hypersocket.triggers;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;

public abstract class MultipleTaskResults extends TaskResult {

	private static final long serialVersionUID = -1287105067476106327L;

	TaskResult[] results;
	public MultipleTaskResults(Object source, Realm currentRealm, Task task, TaskResult... results) {
		super(source, "multiple.results", true, currentRealm, task);
		this.results = results;
	}

	@Override
	public boolean isPublishable() {
		return false;
	}

	public TaskResult[] getResults() {
		return results;
	}
}
