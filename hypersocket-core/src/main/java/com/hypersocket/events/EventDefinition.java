package com.hypersocket.events;

import java.util.HashSet;
import java.util.Set;

public class EventDefinition {

	String resourceKey;
	String resourceBundle;
	Set<String> attributeNames = new HashSet<String>();
	
	public EventDefinition(String resourceBundle, String resourceKey) {
		this.resourceBundle = resourceBundle;
		this.resourceKey = resourceKey;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public String getResourceBundle() {
		return resourceBundle;
	}

	public Set<String> getAttributeNames() {
		return attributeNames;
	}
	
	
}
