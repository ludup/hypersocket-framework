package com.hypersocket.search;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.search.task.SearchResourceTask;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Service
public class SearchableResourceServiceImpl extends AbstractTaskProvider implements SearchableResourceService {

	@Autowired
	ApplicationContext applicationContext;
	

	@PostConstruct
	private void postConstruct() {
		
		
		taskService.registerTaskProvider(this);
	}

	@Override
	public String getResourceBundle() {
		return SearchResourceTask.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return tasks.keySet().toArray(new String[0]);
	}

	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event) throws ValidationException {
	
		return null;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getResultResourceKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
