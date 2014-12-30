package com.hypersocket.triggers.actions.ip;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEventStatus;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;

public class BlockedIPResult extends ActionResult {

	private static final long serialVersionUID = 1931288302204904429L;

	public BlockedIPResult(Object source, Realm currentRealm, TriggerAction action, String ipAddress) {
		super(source, "blocked.ip", SystemEventStatus.SUCCESS, currentRealm, action);
		addAttribute(CommonAttributes.ATTR_IP_ADDRESS, ipAddress);
	}

	
	public BlockedIPResult(Object source, Throwable e,
			Realm currentRealm, TriggerAction action, String ipAddress) {
		super(source, "blocked.ip", e, currentRealm, action);
		addAttribute(CommonAttributes.ATTR_IP_ADDRESS, ipAddress);
	}


	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return BlockIPTriggerAction.RESOURCE_BUNDLE;
	}

}
