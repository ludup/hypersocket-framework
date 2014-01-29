package com.hypersocket.properties;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class AbstractPropertyTemplate {

	String resourceKey;
	String defaultValue;
	String metaData;
	int weight;
	boolean hidden;
	PropertyCategory category;
	
	public String getResourceKey() {
		return resourceKey;
	}

	public int getId() {
		return resourceKey.hashCode();
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public abstract String getValue();
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	@JsonIgnore
	public PropertyCategory getCategory() {
		return category;
	}
	
	public void setCategory(PropertyCategory category) {
		this.category = category;
	}
}
