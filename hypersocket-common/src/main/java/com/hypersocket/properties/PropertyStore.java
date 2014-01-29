package com.hypersocket.properties;

import java.util.List;

public interface PropertyStore {

	public String getPropertyValue(PropertyTemplate template);
	
	public void setProperty(PropertyTemplate property, String value);

	public List<Property> getProperties(String module);

	public void registerTemplate(PropertyTemplate template, String module);

	public PropertyTemplate getPropertyTemplate(String resourceKey);

}
