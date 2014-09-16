package com.hypersocket.triggers;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;

public abstract class ActionResult extends SystemEvent {

	private static final long serialVersionUID = 5664474659342093254L;

	public static final String ATTR_ACTION_NAME = "attr.actionName";
	public static final String ATTR_TRIGGER_NAME = "attr.triggerName";
	
	public ActionResult(Object source, String resourceKey, boolean success,
			Realm currentRealm, TriggerAction action) {
		super(source, resourceKey, success, currentRealm);
		addAttribute(ATTR_TRIGGER_NAME, action.getTrigger().getName());
		addAttribute(ATTR_ACTION_NAME, action.getName());
	}

	public ActionResult(Object source, String resourceKey,
			SystemEventStatus status, Realm currentRealm, TriggerAction action) {
		super(source, resourceKey, status, currentRealm);
		addAttribute(ATTR_TRIGGER_NAME, action.getTrigger().getName());
		addAttribute(ATTR_ACTION_NAME, action.getName());
	}

	public ActionResult(Object source, String resourceKey, Throwable e,
			Realm currentRealm, TriggerAction action) {
		super(source, resourceKey, e, currentRealm);
		addAttribute(ATTR_TRIGGER_NAME, action.getTrigger().getName());
		addAttribute(ATTR_ACTION_NAME, action.getName());
	}

	public abstract boolean isPublishable();

	@Override
	public abstract String getResourceBundle();

}
