package com.hypersocket.tasks.ip.resolve;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class ResolveIPTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = -7793105529302818754L;

	public static final String EVENT_RESOURCE_KEY = "resolveIP.result";
	
	public static final String ATTR_HOSTNAMES = "attr.hostnames";
	public static final String ATTR_IPS = "attr.ips";
	
	public ResolveIPTaskResult(Object source, String[] hostnames, String[] ips,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm, task);
		addAttribute(ATTR_HOSTNAMES, StringUtils.arrayToDelimitedString(hostnames, "\r\n"));
		addAttribute(ATTR_IPS, StringUtils.arrayToDelimitedString(ips, "\r\n"));
	}

	public ResolveIPTaskResult(Object source, String hostname, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_HOSTNAMES, hostname);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return ResolveIPTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
