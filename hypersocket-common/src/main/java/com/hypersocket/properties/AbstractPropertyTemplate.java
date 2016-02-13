package com.hypersocket.properties;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractPropertyTemplate {

	String resourceKey;
	String defaultValue;
	String defaultsToProperty;
	int weight;
	boolean hidden;
	String displayMode;
	boolean readOnly;
	boolean encrypted;
	PropertyCategory category;
	String mapping;
	PropertyStore propertyStore;
	String metaData;
	Map<String,String> attributes = new HashMap<String,String>();
	
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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	@JsonIgnore
	public PropertyCategory getCategory() {
		return category;
	}
	
	public void setCategory(PropertyCategory category) {
		this.category = category;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	
	public void setPropertyStore(PropertyStore propertyStore) {
		this.propertyStore = propertyStore;
	}
	
	@JsonIgnore
	public PropertyStore getPropertyStore() {
		return propertyStore;
	}
	
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isDefaultValuePropertyValue() {
		return defaultsToProperty!=null;
	}
	
	public String getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}

	public String getDefaultsToProperty() {
		return defaultsToProperty;
	}

	public void setDefaultsToProperty(String defaultsToProperty) {
		this.defaultsToProperty = defaultsToProperty;
	}
	
	public Map<String,String> getAttributes() {
		return attributes;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resourceKey == null) ? 0 : resourceKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPropertyTemplate other = (AbstractPropertyTemplate) obj;
		if (resourceKey == null) {
			if (other.resourceKey != null)
				return false;
		} else if (!resourceKey.equals(other.resourceKey))
			return false;
		return true;
	}
	
	
}
