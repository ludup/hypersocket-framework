package com.hypersocket.triggers.actions.email;

import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class EmailActionResult extends ActionResult {

	private static final long serialVersionUID = -5374654828955586879L;

	public static final String EVENT_RESOURCE_KEY = "event.sentEmail";
	
	public EmailActionResult(Object source, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, true, currentRealm);
	}

	public EmailActionResult(Object source, Throwable e, Realm currentRealm) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

}
