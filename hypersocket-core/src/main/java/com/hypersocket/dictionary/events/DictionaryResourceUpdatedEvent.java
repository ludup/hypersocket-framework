package com.hypersocket.dictionary.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.dictionary.Word;
import com.hypersocket.session.Session;

@SuppressWarnings("serial")
public class DictionaryResourceUpdatedEvent extends
		DictionaryResourceEvent {
	public static final String EVENT_RESOURCE_KEY = "dictionary.updated";

	public DictionaryResourceUpdatedEvent(Object source,
			Session session, Word resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public DictionaryResourceUpdatedEvent(Object source,
			Word resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}