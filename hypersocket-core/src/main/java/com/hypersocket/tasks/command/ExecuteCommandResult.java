package com.hypersocket.tasks.command;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.AbstractTaskResult;

public class ExecuteCommandResult extends AbstractTaskResult {

	private static final long serialVersionUID = -7350055708830738608L;

	public static final String ATTR_COMMAND = "attr.command";
	public static final String ATTR_ARGS = "attr.args";
	public static final String ATTR_EXIT_CODE = "attr.exitCode";
	public static final String ATTR_OUTPUT = "attr.output";
	
	public static final String EVENT_RESOURCE_KEY = "executeCommand.result";
	
	public ExecuteCommandResult(Object source, boolean success, Realm currentRealm, Task task, String output, int exitCode, String command, String[] args) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_EXIT_CODE, String.valueOf(exitCode));
		addAttribute(ATTR_OUTPUT, output);
		addAttribute(ATTR_COMMAND, command);
		addAttribute(ATTR_ARGS, StringUtils.arrayToDelimitedString(args, " "));
	}

	public ExecuteCommandResult(Object source, Throwable e,
			Realm currentRealm, Task task, String output, String command, String[] args) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_EXIT_CODE, String.valueOf(-1));
		addAttribute(ATTR_OUTPUT, output);
		addAttribute(ATTR_COMMAND, command);
		addAttribute(ATTR_ARGS, StringUtils.arrayToDelimitedString(args, " "));
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return TaskProviderServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
