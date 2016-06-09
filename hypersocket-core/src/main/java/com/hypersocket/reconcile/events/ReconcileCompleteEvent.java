package com.hypersocket.reconcile.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.Resource;

public class ReconcileCompleteEvent<T extends Resource> extends ReconcileEvent<T> {

	private static final long serialVersionUID = 3838982900684851453L;

	public static final String EVENT_RESOURCE_KEY = "resourceReconcile.completed";
	
	public static final String ATTR_RECONCILE_TIME_MS = "reconcile.timeMs";

	public ReconcileCompleteEvent(Object source, boolean success, Realm realm, ReconcileStartedEvent<T> started) {
		super(source, EVENT_RESOURCE_KEY, success, realm, started.getResource());
		addAttribute(ATTR_RECONCILE_TIME_MS, String.valueOf(getTimestamp() - started.getTimestamp()));
	}
	
	public ReconcileCompleteEvent(Object source,
			Throwable t, Realm realm) {
		super(source, EVENT_RESOURCE_KEY, t, realm);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
