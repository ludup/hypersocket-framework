package com.hypersocket.tasks.reset.profile;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class ResetProfileTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "resetProfile.result";
	
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	
	public ResetProfileTaskResult(Object source, 
			Realm currentRealm, Task task, Principal principal) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm, task);
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getPrincipalName());
	}

	public ResetProfileTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task, String name) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_PRINCIPAL_NAME, name);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return ResetProfileTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
