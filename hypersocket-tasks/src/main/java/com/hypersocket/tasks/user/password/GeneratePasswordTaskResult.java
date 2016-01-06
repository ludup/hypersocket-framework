package com.hypersocket.tasks.user.password;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class GeneratePasswordTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = -1116372315613765218L;

	public static final String EVENT_RESOURCE_KEY = "generatePassword.result";
	
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PASSWORD = "attr.password";
	
	public GeneratePasswordTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, Principal principal, String password) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getPrincipalName());
		addAttribute(ATTR_PASSWORD, password);
	}

	public GeneratePasswordTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task, String principalName) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
		addAttribute(ATTR_PRINCIPAL_NAME, principalName);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return GeneratePasswordTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
