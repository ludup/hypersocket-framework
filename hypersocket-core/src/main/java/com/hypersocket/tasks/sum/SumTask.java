package com.hypersocket.tasks.sum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.count.CountService;
import com.hypersocket.triggers.ValidationException;

@Component
public class SumTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "sumTask";

	public static final String RESOURCE_BUNDLE = "SumTask";
	
	@Autowired
	private SumTaskRepository repository;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService; 

	@Autowired
	private CountService countService; 
	
	public SumTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(SumTaskResult.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { TASK_RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> events)
			throws ValidationException {

		Map<String,Long> counts = new HashMap<>();
		Long sum = 0L;
		boolean reset = repository.getBooleanValue(task, "sum.reset");
		
		try {
			for(String key : repository.getValues(task, "sum.keys")) {
				Long count = countService.sum(currentRealm, false, processTokenReplacements(key, events));
				counts.put(key, count);
				sum += count;
			}

			if(reset) {
				countService.resetKeys(currentRealm, counts.keySet());
			}
			return new SumTaskResult(this, true, currentRealm, task, sum, counts);
		} catch (ResourceException e) {
			return new SumTaskResult(this, e, currentRealm, task);
		}
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { SumTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	public String getResultResourceKey() {
		return SumTaskResult.EVENT_RESOURCE_KEY;
	}

}
