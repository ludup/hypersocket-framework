package com.hypersocket.automation.events;

import com.hypersocket.automation.AutomationResource;
import com.hypersocket.automation.AutomationResourceServiceImpl;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;

public class AutomationTaskStartedEvent extends
		SystemEvent {

	private static final long serialVersionUID = -3063880990945502517L;

	public static final String EVENT_RESOURCE_KEY = "automation.started";
	
	public AutomationTaskStartedEvent(Object source,
			AutomationResource resource) {
		super(source, EVENT_RESOURCE_KEY, true, resource.getRealm());
	}

	public AutomationTaskStartedEvent(Object source,
			Realm realm, Throwable e) {
		super(source, EVENT_RESOURCE_KEY, e, realm);
	}

	@Override
	public String getResourceBundle() {
		return AutomationResourceServiceImpl.RESOURCE_BUNDLE;
	}

}
