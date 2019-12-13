package com.hypersocket.dictionary.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.dictionary.Word;
import com.hypersocket.events.CommonAttributes;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;
import com.hypersocket.utils.HypersocketUtils;

@SuppressWarnings("serial")
public class DictionaryResourceEvent extends SessionEvent {

	public static final String EVENT_RESOURCE_KEY = "dictionary.event";
	
	public static final String ATTR_REALM_NAME = CommonAttributes.ATTR_REALM_NAME;
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	public static final String ATTR_RESOURCE_ID = "attr.resourceId";
	public static final String ATTR_OLD_RESOURCE_NAME = "attr.oldResourceName";
	public static final String ATTR_CREATED = "attr.created";
	public static final String ATTR_LAST_MODIFIED = "attr.lastModified";

	private Word resource;
	
	public DictionaryResourceEvent(Object source, String resourceKey,
			Session session, Word resource) {
		super(source, resourceKey, true, session);
		
		this.resource = resource;
		
		addAttribute(ATTR_RESOURCE_ID, Long.toString(resource.getId()));
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		addAttribute(ATTR_CREATED, HypersocketUtils.formatDateTime(resource.getCreateDate()));
		addAttribute(ATTR_LAST_MODIFIED, HypersocketUtils.formatDateTime(resource.getModifiedDate()));
	}

	public DictionaryResourceEvent(Object source, String resourceKey,
			Word resource, Throwable e, Session session) {
		super(source, resourceKey, e, session);
		
		this.resource = resource;
	}
	
	public Word getResource() {
		return resource;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
