package com.hypersocket.triggers.events;

import com.hypersocket.realm.events.RealmResourceEvent;
import com.hypersocket.session.Session;
import com.hypersocket.triggers.TriggerResource;

public class TriggerResourceEvent extends RealmResourceEvent {

//	public static final String ATTR_NAME = "attr.name";
	
	public TriggerResourceEvent(Object source, String resourceKey,
			Session session, TriggerResource resource) {
		super(source, resourceKey, true, session, resource);

		/**
		 * TODO add attributes of your resource here. Make sure all attributes
		 * have a constant string definition like the commented out example above,
		 * its important for its name to start with ATTR_ as this is picked up during 
		 * the registration process
		 */
	}

	public TriggerResourceEvent(Object source, String resourceKey,
			TriggerResource resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		

	}

}
