package com.hypersocket.reconcile.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.reconcile.AbstractReconcileServiceImpl;
import com.hypersocket.resource.Resource;

public class ReconcileEvent<T extends Resource> extends SystemEvent {

	public static final String EVENT_RESOURCE_KEY = "resourceReconcile.event";
	
	public static final String ATTR_RESOURCE_NAME = CommonAttributes.ATTR_RESOURCE_NAME;
	
	private static final long serialVersionUID = -7027070764958878422L;

	T resource;
	
	public ReconcileEvent(Object source, String resourceKey, boolean success, Realm realm, T resource) {
		super(source, resourceKey, success, realm);
		addAttribute(ATTR_RESOURCE_NAME, resource.getName());
		this.resource = resource;
	}

	public ReconcileEvent(Object source, String resourceKey, Throwable e, Realm realm) {
		super(source, resourceKey, e, realm);
	}

	@Override
	public String getResourceBundle() {
		return AbstractReconcileServiceImpl.RESOURCE_BUNDLE;
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

	public T getResource() {
		return resource;
	}
}
