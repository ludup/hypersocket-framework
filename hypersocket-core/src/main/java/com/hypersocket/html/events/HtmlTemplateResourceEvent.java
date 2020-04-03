package com.hypersocket.html.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.session.Session;

public class HtmlTemplateResourceEvent extends ResourceEvent {

	private static final long serialVersionUID = 4286206245793654981L;
	
	public static final String EVENT_RESOURCE_KEY = "htmlTemplate.event";
	
	public HtmlTemplateResourceEvent(Object source, String resourceKey,
			Session session, HtmlTemplateResource resource) {
		super(source, resourceKey, true, session, resource);
	}

	public HtmlTemplateResourceEvent(Object source, String resourceKey,
			HtmlTemplateResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
