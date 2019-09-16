package com.hypersocket.tasks.count;

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
import com.hypersocket.triggers.ValidationException;

@Component
public class CountTaskTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "countTaskTask";

	public static final String RESOURCE_BUNDLE = "CountTaskTask";

	@Autowired
	private CountTaskTaskRepository repository;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private CountService countService;

	public CountTaskTask() {
	}

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(CountTaskTaskResult.class, RESOURCE_BUNDLE);
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
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {

	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> events) throws ValidationException {

		String uniqueKey = processTokenReplacements(repository.getValue(task, "countTask.resourceKey"), events);
		Long adjustment = Long
				.parseLong(processTokenReplacements(repository.getValue(task, "countTask.increment"), events));

		try {
			countService.adjustCount(currentRealm, uniqueKey, adjustment);
			return new CountTaskTaskResult(this, true, currentRealm, task, uniqueKey, adjustment,
					countService.sum(currentRealm, false, uniqueKey));
		} catch (ResourceException e) {
			return new CountTaskTaskResult(this, e, currentRealm, task);
		}

	}

	public String[] getResultResourceKeys() {
		return new String[] { CountTaskTaskResult.EVENT_RESOURCE_KEY };
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
		return CountTaskTaskResult.EVENT_RESOURCE_KEY;
	}

}
