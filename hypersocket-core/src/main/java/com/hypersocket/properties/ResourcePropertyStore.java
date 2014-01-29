package com.hypersocket.properties;

import com.hypersocket.resource.Resource;

public interface ResourcePropertyStore extends PropertyStore {

	public String getPropertyValue(AbstractPropertyTemplate template, Resource resource);
	
	public void setPropertyValue(AbstractPropertyTemplate template, Resource resource, String value);
}
