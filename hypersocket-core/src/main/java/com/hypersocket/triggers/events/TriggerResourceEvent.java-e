package com.hypersocket.triggers.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

public class TriggerResourceEvent extends ResourceEvent {

	public static final String EVENT_RESOURCE_KEY = "trigger.event";
	
	private static final long serialVersionUID = 2854425020068386496L;

	public static final String ATTR_EVENT = "attr.event";
	public static final String ATTR_TASK = "attr.task";
	
	public TriggerResourceEvent(Object source, String resourceKey,
			Session session, TriggerResource resource) {
		super(source, resourceKey, true, session, resource);
		addAttribute(ATTR_EVENT, I18NServiceImpl.tagForConversion(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, 
				resource.getEvent()));
		addAttribute(ATTR_TASK, I18NServiceImpl.tagForConversion(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, 
				resource.getResourceKey()));
	}

	public TriggerResourceEvent(Object source, String resourceKey,
			TriggerResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		addAttribute(ATTR_EVENT, I18NServiceImpl.tagForConversion(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, 
				resource.getEvent()));
		addAttribute(ATTR_TASK, I18NServiceImpl.tagForConversion(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, 
				resource.getResourceKey()));
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
