package com.hypersocket.tasks.command;

import org.springframework.util.StringUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.TaskResult;

public class ExecuteCommandResult extends TaskResult {

	private static final long serialVersionUID = -7350055708830738608L;

	public static final String ATTR_COMMAND = "attr.command";
	public static final String ATTR_ARGS = "attr.args";
	public static final String ATTR_EXIT_CODE = "attr.exitCode";
	public static final String ATTR_OUTPUT = "attr.output";
	
	public ExecuteCommandResult(Object source, String resourceKey,
			boolean success, Realm currentRealm, Task task, String output, int exitCode, String command, String[] args) {
		super(source, resourceKey, success, currentRealm, task);
		addAttribute(ATTR_EXIT_CODE, String.valueOf(exitCode));
		addAttribute(ATTR_OUTPUT, output);
		addAttribute(ATTR_COMMAND, command);
		addAttribute(ATTR_ARGS, StringUtils.arrayToDelimitedString(args, " "));
	}

	public ExecuteCommandResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, Task task, String output, String command, String[] args) {
		super(source, resourceKey, e, currentRealm, task);
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

}
