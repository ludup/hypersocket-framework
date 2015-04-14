package com.hypersocket.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.realm.Realm;

public abstract class SystemEvent extends AbstractEvent {

	private static final long serialVersionUID = -2862861933633430347L;

	public static final String EVENT_RESOURCE_KEY = "system.event";
	public static final String EVENT_NAMESPACE = "";
	
	public static final String ATTR_EXCEPTION_TEXT = "attr.exception";
	
	String resourceKey;
	SystemEventStatus status;
	Throwable exception;
	Realm currentRealm;
	protected boolean hidden;
	
	Map<String,String> attributes = new HashMap<String,String>();
	
	public SystemEvent(Object source, String resourceKey, boolean success, Realm currentRealm) {
		super(source);
		this.status = success ? SystemEventStatus.SUCCESS : SystemEventStatus.FAILURE;
		this.resourceKey = resourceKey;
		this.currentRealm = currentRealm;
	}
	
	public SystemEvent(Object source, String resourceKey, SystemEventStatus status, Realm currentRealm) {
		super(source);
		this.status = status;
		this.resourceKey = resourceKey;
		this.currentRealm = currentRealm;
	}
	
	public SystemEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source);
		this.status = SystemEventStatus.FAILURE;
		this.resourceKey = resourceKey;
		this.exception = e;
		this.currentRealm = currentRealm;
		addAttribute(ATTR_EXCEPTION_TEXT, e.getMessage());
	}
	
	public Realm getCurrentRealm() {
		return currentRealm;
	}
	
	public abstract String getResourceBundle();

	protected void buildAttributes() {
		
	}
	
	public Throwable getException() {
		return exception;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public boolean isSuccess() {
		return status==SystemEventStatus.SUCCESS;
	}
	
	public SystemEventStatus getStatus() {
		return status;
	}
	
	public SystemEvent addAllAttributes(Map<String,String> attributes) {
		this.attributes.putAll(attributes);
		return this;
	}
	
	public SystemEvent addAttribute(String name, String value) {
		attributes.put(name,value);
		return this;
	}
	
	public SystemEvent addAttribute(String name, Object value) {
		attributes.put(name,String.valueOf(value));
		return this;
	}
	
	public String getAttribute(String name) {
		return attributes.get(name);
	}
	
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	public Map<String,String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
	
	public boolean isHidden() {
		return hidden;
	}

}

