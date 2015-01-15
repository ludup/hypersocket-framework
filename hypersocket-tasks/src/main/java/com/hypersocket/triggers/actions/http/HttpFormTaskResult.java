package com.hypersocket.triggers.actions.http;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class HttpFormTaskResult extends TaskResult {

	private static final long serialVersionUID = -5846061448001973460L;

	public HttpFormTaskResult(Object source, Realm currentRealm, Task task,
			String method, String url, String[] variables) {
		super(source, "httpForm", SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute("httpForm.method", method);
		addAttribute("httpForm.url", url);
		addAttribute("httpForm.variables", variables);
	}

	public HttpFormTaskResult(Object source, Throwable e, Realm currentRealm,
			Task task, String method, String url, String[] variables) {
		super(source, "blocked.ip", e, currentRealm, task);
		addAttribute("httpForm.method", method);
		addAttribute("httpForm.url", url);
		addAttribute("httpForm.variables", variables);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return HttpFormTask.RESOURCE_BUNDLE;
	}

}
