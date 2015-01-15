package com.hypersocket.tasks.command;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.tasks.email.EmailTaskResult;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class ExecuteCommandTask extends AbstractTaskProvider {

	
	public static final String EXECUTE_COMMAND_TASK = "executeCommand";
	
	@Autowired
	ExecuteCommandRepository repository; 
	
	@Autowired
	TaskProviderService taskService; 
	
	@Autowired
	EventService eventService; 
	
	public ExecuteCommandTask() {
	}

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		eventService.registerEvent(EmailTaskResult.class,
				TaskProviderServiceImpl.RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return TaskProviderServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { EXECUTE_COMMAND_TASK };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {

		return null;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
