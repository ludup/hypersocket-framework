package com.hypersocket.tasks;

import java.util.List;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public interface DynamicResultsTaskProvider extends TaskProvider {
	
	TaskResult execute(DynamicTaskExecutionContext context, Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException;
}
