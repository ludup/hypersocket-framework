package com.hypersocket.properties;


public interface PropertyStore {

	String getPropertyValue(PropertyTemplate template);
	
	void setProperty(PropertyTemplate property, String value);

	void registerTemplate(PropertyTemplate template, String module);

	PropertyTemplate getPropertyTemplate(String resourceKey);

	boolean isDefaultStore();

}
