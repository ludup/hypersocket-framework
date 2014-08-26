package com.hypersocket.triggers.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.hypersocket.properties.json.PropertyItem;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerActionUpdate {

	Long id;
	String name;
	String resourceKey;
	PropertyItem[] properties;
	
	public TriggerActionUpdate() {
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PropertyItem[] getProperties() {
		return properties;
	}
	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}

	public String getResourceKey() {
		return resourceKey;
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	
}
