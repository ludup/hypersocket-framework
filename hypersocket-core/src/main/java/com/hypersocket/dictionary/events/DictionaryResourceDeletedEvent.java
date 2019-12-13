package com.hypersocket.dictionary.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.dictionary.Word;
import com.hypersocket.session.Session;

@SuppressWarnings("serial")
public class DictionaryResourceDeletedEvent extends
		DictionaryResourceEvent {

	/**
	 * TODO You typically add attributes to the base DictionaryResourceEvent
	 * class so these can be reused across all resource events.
	 */
	public static final String EVENT_RESOURCE_KEY = "dictionary.deleted";

	public DictionaryResourceDeletedEvent(Object source,
			Session session, Word resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public DictionaryResourceDeletedEvent(Object source,
			Word resource, Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
