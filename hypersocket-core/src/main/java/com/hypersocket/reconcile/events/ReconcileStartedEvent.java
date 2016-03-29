package com.hypersocket.reconcile.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;

public class ReconcileStartedEvent<T extends Resource> extends ReconcileEvent<T> {

	private static final long serialVersionUID = 6524528273858765454L;
	
	public static final String EVENT_RESOURCE_KEY = "reconcile.started";
	
	public ReconcileStartedEvent(Object source,
			boolean success, Realm realm, T resource) {
		super(source, EVENT_RESOURCE_KEY, success, realm, resource);
	}
	
	public ReconcileStartedEvent(Object source,
			Throwable t, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, t, realm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
