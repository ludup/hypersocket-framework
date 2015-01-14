package com.hypersocket.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.triggers.TriggerResourceRepository;

@Service
public class TaskProviderServiceImpl extends AuthenticatedServiceImpl implements TaskProviderService {

	public static final String RESOURCE_BUNDLE = "TaskProviderService";
	@Autowired
	TriggerResourceRepository repository;
	
	Map<String, TaskProvider> registeredTasks = new HashMap<String, TaskProvider>();
	List<String> triggerTasks = new ArrayList<String>();
	List<String> automationTasks = new ArrayList<String>();
	
	@Override
	public void registerActionProvider(TaskProvider action) {
		repository.registerActionRepository(action);
		for (String resourceKey : action.getResourceKeys()) {
			registeredTasks.put(resourceKey, action);
			if(action.supportsAutomation()) {
				automationTasks.add(resourceKey);
			}
			if(action.supportsTriggers()) {
				triggerTasks.add(resourceKey);
			}
		}
	}
	@Override
	public TaskProvider getActionProvider(String resourceKey) {
		return registeredTasks.get(resourceKey);
	}


	@Override
	public List<String> getTriggerTasks() {
		return triggerTasks;
	}
	
	@Override
	public List<String> getAutomationTasks() {
		return automationTasks;
	}
	@Override
	public TaskProvider getActionProvider(Task task) {
		return getActionProvider(task.getResourceKey());
	}

}
