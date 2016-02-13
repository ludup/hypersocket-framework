package com.hypersocket.tasks.command;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.utils.CommandExecutor;

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

		eventService.registerEvent(ExecuteCommandResult.class,
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
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		String command = processTokenReplacements(repository.getValue(task,
				"command.exe"), event);
		String[] args = repository.getValues(task, "command.args");
		for(int i=0;i<args.length;i++) {
			args[i] = processTokenReplacements(args[i], event);
		}
		CommandExecutor exe = new CommandExecutor(command);
		exe.addArgs(args);

		try {
			int result = exe.execute();

			return new ExecuteCommandResult(this, 
					result == 0, currentRealm, task,
					exe.getCommandOutput(), result, command, args);
		} catch (IOException e) {
			return new ExecuteCommandResult(this, e,
					currentRealm, task, exe.getCommandOutput(), command, args);
		}
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { ExecuteCommandResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
