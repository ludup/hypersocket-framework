package com.hypersocket.triggers.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.PropertyItem;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerActionUpdate {

	private Long id;
	private boolean newAction;
	private String name;
	private String resourceKey;
	private PropertyItem[] properties;
	
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

	public boolean isNewAction() {
		return newAction;
	}

	public void setNewAction(boolean newAction) {
		this.newAction = newAction;
	}
	
	
}
