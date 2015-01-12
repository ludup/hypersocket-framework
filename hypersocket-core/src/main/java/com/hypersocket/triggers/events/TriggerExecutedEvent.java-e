package com.hypersocket.triggers.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class TriggerExecutedEvent extends SystemEvent {

	private static final long serialVersionUID = 6236130214109189906L;

	public static final String EVENT_RESOURCE_KEY = "event.triggerExecuted";
	
	public static final String ATTR_TRIGGER_NAME = "attr.triggerName";
	
	public TriggerExecutedEvent(Object source, TriggerResource trigger) {
		super(source, EVENT_RESOURCE_KEY, true, trigger.getRealm());
		addAttribute(ATTR_TRIGGER_NAME, trigger.getName());
	}

	public TriggerExecutedEvent(Object source, 
			TriggerResource trigger, Throwable e) {
		super(source, EVENT_RESOURCE_KEY, e, trigger.getRealm());
		addAttribute(ATTR_TRIGGER_NAME, trigger.getName());
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
