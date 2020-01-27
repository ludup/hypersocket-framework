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
	private List<String> triggerTasks = new ArrayList<String>();
	private List<String> nonSystemTriggerTasks = new ArrayList<String>();
	private List<String> realmTriggerTasks = new ArrayList<String>();
	private List<String> automationTasks = new ArrayList<String>();
	private List<String> nonSystemAutomationTasks = new ArrayList<String>();
	private List<String> realmAutomationTasks = new ArrayList<String>();
	
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
					 realmAutomationTasks.add(resourceKey);
				} else {
					automationTasks.add(resourceKey);
					if(!action.isSystem()) {
						nonSystemAutomationTasks.add(resourceKey);
					}
				}
			}
			
			if(action.supportsTriggers()) {
				if(action.isRealmTask()) {
					realmTriggerTasks.add(resourceKey);
				} else {
					triggerTasks.add(resourceKey);
					if(!action.isSystem()) {
						nonSystemTriggerTasks.add(resourceKey);
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
	public List<String> getTriggerTasks() {
		List<String> results = new ArrayList<String>();
		results.addAll(getCurrentRealm().isSystem() || !isDisableSystemTasks() ? triggerTasks : nonSystemTriggerTasks);	
		for(String resourceKey : realmTriggerTasks) {
			if((registeredTasks.get(resourceKey)).isRealmSupported(getCurrentRealm())) {
				results.add(resourceKey);
			}
		}
		return results;
	}
	
	@Override
	public List<String> getAutomationTasks() {
		List<String> results = new ArrayList<String>();
		results.addAll(getCurrentRealm().isSystem() || !isDisableSystemTasks() ? automationTasks : nonSystemAutomationTasks);
		for(String resourceKey : realmTriggerTasks) {
			if((registeredTasks.get(resourceKey)).isRealmSupported(getCurrentRealm())) {
				results.add(resourceKey);
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
