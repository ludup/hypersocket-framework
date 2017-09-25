package com.hypersocket.properties;

import java.util.Collection;

import com.hypersocket.resource.SimpleResource;

public interface ResourcePropertyStore extends XmlTemplatePropertyStore {

	String getPropertyValue(AbstractPropertyTemplate template, SimpleResource resource);
	
	void setPropertyValue(AbstractPropertyTemplate template, SimpleResource resource, String value);

	boolean hasPropertyValueSet(AbstractPropertyTemplate template, SimpleResource resource);
	
	Collection<String> getPropertyNames();

	String getDecryptedValue(AbstractPropertyTemplate template, SimpleResource resource);

}
