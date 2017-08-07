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
	List<String> nonSystemTriggerTasks = new ArrayList<String>();
	List<String> automationTasks = new ArrayList<String>();
	List<String> nonSystemAutomationTasks = new ArrayList<String>();
	
	boolean disableSystemTasks = false;
	
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
				if(!action.isSystem()) {
					nonSystemAutomationTasks.add(resourceKey);
				}
			}
			
			if(action.supportsTriggers()) {
				triggerTasks.add(resourceKey);
				if(!action.isSystem()) {
					nonSystemTriggerTasks.add(resourceKey);
				}
			}
		}
	}
	
	@Override
	public TaskProvider getTaskProvider(String resourceKey) {
		return registeredTasks.get(resourceKey);
	}

	@Override
	public List<String> getTriggerTasks() {
		return getCurrentRealm().isSystem() || !isDisableSystemTasks() ? triggerTasks : nonSystemTriggerTasks;
	}
	
	@Override
	public List<String> getAutomationTasks() {
		return getCurrentRealm().isSystem() || !isDisableSystemTasks() ? automationTasks : nonSystemAutomationTasks;
	}
	
	@Override
	public TaskProvider getTaskProvider(Task task) {
		return getTaskProvider(task.getResourceKey());
	}

	@Override
	public boolean isDisableSystemTasks() {
		return disableSystemTasks;
	}

	@Override
	public void setDisableSystemTasks(boolean disableSystemTasks) {
		this.disableSystemTasks = disableSystemTasks;
	}

}
