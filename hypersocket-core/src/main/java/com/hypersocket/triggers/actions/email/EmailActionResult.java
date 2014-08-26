package com.hypersocket.triggers.actions.email;

import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class EmailActionResult extends ActionResult {

	private static final long serialVersionUID = -5374654828955586879L;

	public EmailActionResult(Object source, Realm currentRealm) {
		super(source, "email.result", true, currentRealm);
	}

	public EmailActionResult(Object source, Throwable e, Realm currentRealm) {
		super(source, "email.result", e, currentRealm);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

}
