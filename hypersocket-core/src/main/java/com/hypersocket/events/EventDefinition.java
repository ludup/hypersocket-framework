package com.hypersocket.events;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

public class EventDefinition {

	String resourceKey;
	String resourceBundle;
	Set<String> attributeNames = new HashSet<String>();
	EventPropertyCollector propertyCollector;
	
	public EventDefinition(String resourceBundle, String resourceKey, EventPropertyCollector propertyCollector) {
		this.resourceBundle = resourceBundle;
		this.resourceKey = resourceKey;
		this.propertyCollector = propertyCollector;
	}
	
	public EventDefinition(EventDefinition def, Set<String> propertyNames) {
		this.resourceBundle = def.resourceBundle;
		this.resourceKey = def.resourceKey;
		this.attributeNames.addAll(def.attributeNames);
		this.attributeNames.addAll(propertyNames);
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

	@JsonIgnore
	public EventPropertyCollector getPropertyCollector() {
		return propertyCollector;
	}
	
	
}
