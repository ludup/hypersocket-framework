package com.hypersocket.events;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EventDefinition {

	String i18nNamespace;
	String resourceKey;
	String resourceBundle;
	Set<String> attributeNames = new HashSet<String>();
	EventPropertyCollector propertyCollector;
	
	public EventDefinition(String resourceBundle, String resourceKey, String i18nNamespace,  EventPropertyCollector propertyCollector) {
		this.resourceBundle = resourceBundle;
		this.i18nNamespace = i18nNamespace;
		this.resourceKey = resourceKey;
		this.propertyCollector = propertyCollector;
	}
	
	public EventDefinition(EventDefinition def, String i18nNamespace, Set<String> propertyNames) {
		this.resourceBundle = def.resourceBundle;
		this.resourceKey = def.resourceKey;
		this.i18nNamespace = i18nNamespace;
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
	
	public String getI18nNamespace() {
		return i18nNamespace;
	}
	
	
}
