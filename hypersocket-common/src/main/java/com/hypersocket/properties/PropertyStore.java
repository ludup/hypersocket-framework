package com.hypersocket.properties;


public interface PropertyStore {

	public String getPropertyValue(PropertyTemplate template);
	
	public void setProperty(PropertyTemplate property, String value);

	public void registerTemplate(PropertyTemplate template, String module);

	public PropertyTemplate getPropertyTemplate(String resourceKey);

	public boolean isDefaultStore();

}
