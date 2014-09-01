package com.hypersocket.triggers;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;

public abstract class ActionResult extends SystemEvent {

	private static final long serialVersionUID = 5664474659342093254L;

	public ActionResult(Object source, String resourceKey, boolean success,
			Realm currentRealm) {
		super(source, resourceKey, success, currentRealm);
	}

	public ActionResult(Object source, String resourceKey,
			SystemEventStatus status, Realm currentRealm) {
		super(source, resourceKey, status, currentRealm);
	}

	public ActionResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm) {
		super(source, resourceKey, e, currentRealm);
	}

	public abstract boolean isPublishable();

	@Override
	public abstract String getResourceBundle();

}
