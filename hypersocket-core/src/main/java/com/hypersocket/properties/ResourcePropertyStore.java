package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.AbstractResource;

public interface ResourcePropertyStore extends XmlTemplatePropertyStore {

	public String getPropertyValue(AbstractPropertyTemplate template, AbstractResource resource);
	
	public void setPropertyValue(AbstractPropertyTemplate template, AbstractResource resource, String value);

	public boolean hasPropertyValueSet(AbstractPropertyTemplate template, AbstractResource resource);
	
	Collection<String> getPropertyNames();
}
