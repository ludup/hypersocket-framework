package com.hypersocket.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationEvent;

import com.hypersocket.realm.Realm;

public abstract class SystemEvent extends ApplicationEvent {

	private static final long serialVersionUID = -2862861933633430347L;

	public static final String ATTR_EXCEPTION_TEXT = "attr.exception";
	
	String resourceKey;
	boolean success;
	Throwable exception;
	Realm currentRealm;
	
	Map<String,String> attributes = new HashMap<String,String>();
	
	public SystemEvent(Object source, String resourceKey, boolean success, Realm currentRealm) {
		super(source);
		this.success = success;
		this.resourceKey = resourceKey;
		this.currentRealm = currentRealm;
	}
	
	public SystemEvent(Object source, String resourceKey, Throwable e, Realm currentRealm) {
		super(source);
		this.success = false;
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
		return success;
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

}

