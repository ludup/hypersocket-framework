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
	private TriggerResourceRepository repository;
	
	@Autowired
	private I18NService i18nService; 
	
	private Map<String, TaskProvider> registeredTasks = new HashMap<String, TaskProvider>();
	private List<TaskDefinition> triggerTasks = new ArrayList<>();
	private List<TaskDefinition> nonSystemTriggerTasks = new ArrayList<>();
	private List<TaskDefinition> realmTriggerTasks = new ArrayList<>();
	private List<TaskDefinition> automationTasks = new ArrayList<>();
	private List<TaskDefinition> nonSystemAutomationTasks = new ArrayList<>();
	private List<TaskDefinition> realmAutomationTasks = new ArrayList<>();
	
	private boolean disableSystemTasks = false;
	
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
				if(action.isRealmTask()) {
					 realmAutomationTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
				} else {
					automationTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
					if(!action.isSystem()) {
						nonSystemAutomationTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
					}
				}
			}
			
			if(action.supportsTriggers()) {
				if(action.isRealmTask()) {
					realmTriggerTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
				} else {
					triggerTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
					if(!action.isSystem()) {
						nonSystemTriggerTasks.add(new TaskDefinition(resourceKey, action.getDisplayMode()));
					}
				}
			}
		}
	}
	
	@Override
	public TaskProvider getTaskProvider(String resourceKey) {
		return registeredTasks.get(resourceKey);
	}

	@Override
	public List<TaskDefinition> getTriggerTasks() {
		List<TaskDefinition> results = new ArrayList<>();
		results.addAll(getCurrentRealm().isSystem() || !isDisableSystemTasks() ? triggerTasks : nonSystemTriggerTasks);	
		for(TaskDefinition task : realmTriggerTasks) {
			if((registeredTasks.get(task.getResourceKey())).isRealmSupported(getCurrentRealm())) {
				results.add(task);
			}
		}
		return results;
	}
	
	@Override
	public List<TaskDefinition> getAutomationTasks() {
		List<TaskDefinition> results = new ArrayList<>();
		results.addAll(getCurrentRealm().isSystem() || !isDisableSystemTasks() ? automationTasks : nonSystemAutomationTasks);
		for(TaskDefinition task : realmTriggerTasks) {
			if((registeredTasks.get(task.getResourceKey())).isRealmSupported(getCurrentRealm())) {
				results.add(task);
			}
		}
		return results;
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
