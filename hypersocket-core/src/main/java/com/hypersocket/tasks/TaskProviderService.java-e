package com.hypersocket.tasks;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;

public interface TaskProviderService extends AuthenticatedService {

	TaskProvider getTaskProvider(String resourceKey);

	void registerTaskProvider(TaskProvider action);

	TaskProvider getTaskProvider(Task task);

	List<String> getTriggerTasks();

	List<String> getAutomationTasks();

}
