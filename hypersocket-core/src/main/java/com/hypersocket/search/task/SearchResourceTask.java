package com.hypersocket.search.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;
import com.hypersocket.search.Searchable;
import com.hypersocket.search.SearchableResourceProvider;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.MultipleTaskResults;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class SearchResourceTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "searchResourceTask";

	public static final String RESOURCE_BUNDLE = "SearchResourceTask";
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	SearchResourceTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	Map<String,SearchableResourceProvider> tasks = new HashMap<String,SearchableResourceProvider>();
	
	
	public SearchResourceTask() {
	}
	
	@PostConstruct
	private void postConstruct() {

		Map<String,Object> beans = applicationContext.getBeansWithAnnotation(Searchable.class);
		
		for(Object bean : beans.values()) {
			if(bean instanceof SearchableResourceProvider) {
				SearchableResourceProvider provider = (SearchableResourceProvider) bean;
				tasks.put(provider.getSearchResourceKey(), provider);
			}
		}
		
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(SearchResourceTaskResult.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return tasks.keySet().toArray(new String[0]);
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		List<SearchResourceTaskResult> results = new ArrayList<SearchResourceTaskResult>();
		
		
		 
		
		return new MultipleTaskResults(this, currentRealm, task, results.toArray(new TaskResult[0]));

	}
	
	public String[] getResultResourceKeys() {
		return new String[] { SearchResourceTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
