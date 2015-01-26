package com.hypersocket.triggers.actions.http;

import org.springframework.util.StringUtils;

import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TaskResult;

public class HttpFormTaskResult extends TaskResult {

	private static final long serialVersionUID = -5846061448001973460L;

	public static final String EVENT_RESOURCE_KEY = "httpForm.result";
	
	public static final String ATTR_HTTP_METHOD = "attr.method";
	public static final String ATTR_URL = "attr.url";
	public static final String ATTR_VARIABLES = "attr.variables";
	
	public static final String ATTR_RESULT = "attr.responseCode";
	public static final String ATTR_CONTENT = "attr.content";
	
	public HttpFormTaskResult(Object source, Realm currentRealm, Task task,
			String method, String url, String[] variables, int responseCode, String content) {
		super(source, EVENT_RESOURCE_KEY, SystemEventStatus.SUCCESS, currentRealm, task);
		addAttribute(ATTR_HTTP_METHOD, method);
		addAttribute(ATTR_URL, url);
		addAttribute(ATTR_VARIABLES, StringUtils.arrayToDelimitedString(variables, "&"));
		addAttribute(ATTR_RESULT, responseCode);
		addAttribute(ATTR_CONTENT, content);
	}

	public HttpFormTaskResult(Object source, Throwable e, Realm currentRealm,
			Task task, String method, String url, String[] variables) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_HTTP_METHOD, method);
		addAttribute(ATTR_URL, url);
		addAttribute(ATTR_VARIABLES, StringUtils.arrayToDelimitedString(variables, "&"));
	}

	public HttpFormTaskResult(Object source, Throwable e, Realm currentRealm,
			Task task, String method, String url, String[] variables,
			int responseCode, String content) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_HTTP_METHOD, method);
		addAttribute(ATTR_URL, url);
		addAttribute(ATTR_VARIABLES, StringUtils.arrayToDelimitedString(variables, "&"));
		addAttribute(ATTR_RESULT, responseCode);
		addAttribute(ATTR_CONTENT, content);
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
