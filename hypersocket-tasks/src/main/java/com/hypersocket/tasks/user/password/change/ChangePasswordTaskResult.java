package com.hypersocket.tasks.user.password.change;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class ChangePasswordTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "changePassword.result";
	
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	public static final String ATTR_PASSWORD = "attr.password";
	public static final String ATTR_GENERATED = "attr.generated";
	
	public ChangePasswordTaskResult(Object source, 
			Realm currentRealm, Task task, Principal principal, String password, boolean generated) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm, task);
		addAttribute(ATTR_PRINCIPAL_NAME, principal.getPrincipalName());
		addAttribute(ATTR_GENERATED, String.valueOf(generated));
		addAttribute(ATTR_PASSWORD, password);
	}

	public ChangePasswordTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return ChangePasswordTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
