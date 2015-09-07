package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.AbstractResource;

public interface ResourcePropertyStore extends XmlTemplatePropertyStore {

	String getPropertyValue(AbstractPropertyTemplate template, AbstractResource resource);
	
	void setPropertyValue(AbstractPropertyTemplate template, AbstractResource resource, String value);

	boolean hasPropertyValueSet(AbstractPropertyTemplate template, AbstractResource resource);
	
	Collection<String> getPropertyNames();

	String getDecryptedValue(AbstractPropertyTemplate template, AbstractResource resource);
}
