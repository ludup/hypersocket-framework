package com.hypersocket.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.triggers.TriggerResourceRepository;

@Service
public class TaskProviderServiceImpl extends AbstractAuthenticatedServiceImpl implements TaskProviderService {

	public static final String RESOURCE_BUNDLE = "TaskProviderService";
	
	@Autowired
	TriggerResourceRepository repository;
	
	@Autowired
	I18NService i18nService; 
	
	Map<String, TaskProvider> registeredTasks = new HashMap<String, TaskProvider>();
	List<String> triggerTasks = new ArrayList<String>();
	List<String> automationTasks = new ArrayList<String>();
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}
	
	@Override
	public void registerTaskProvider(TaskProvider action) {
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
	public TaskProvider getTaskProvider(String resourceKey) {
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
	public TaskProvider getTaskProvider(Task task) {
		return getTaskProvider(task.getResourceKey());
	}

}
