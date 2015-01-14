package com.hypersocket.tasks;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;

public interface TaskProviderService extends AuthenticatedService {

	TaskProvider getActionProvider(String resourceKey);

	void registerActionProvider(TaskProvider action);

	TaskProvider getActionProvider(Task task);

	List<String> getTriggerTasks();

	List<String> getAutomationTasks();

}
