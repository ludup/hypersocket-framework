package com.hypersocket.html.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.session.Session;

public class HtmlTemplateResourceUpdatedEvent extends
		HtmlTemplateResourceEvent {

	private static final long serialVersionUID = -2920128571097905328L;

	public static final String EVENT_RESOURCE_KEY = "htmlTemplate.updated";

	public HtmlTemplateResourceUpdatedEvent(Object source,
			Session session, HtmlTemplateResource resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public HtmlTemplateResourceUpdatedEvent(Object source,
			HtmlTemplateResource resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
