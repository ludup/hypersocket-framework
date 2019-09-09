package com.hypersocket.tasks.email.ban;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;

public class BanEmailAddressTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "banEmailAddress.result";
	
	public static final String ATTR_EMAIL = "attr.email";
	public static final String ATTR_PRINCIPAL_NAME = CommonAttributes.ATTR_PRINCIPAL_NAME;
	
	public BanEmailAddressTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, String email, String name) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute(ATTR_EMAIL, email);
		addAttribute(ATTR_PRINCIPAL_NAME, name);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return BanEmailAddressTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
