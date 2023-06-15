package com.hypersocket.properties;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.hypersocket.resource.SimpleResource;

public class DatabasePropertyStore extends AbstractResourcePropertyStore {

	PropertyRepository repository;
	
	public DatabasePropertyStore(PropertyRepository repository, ApplicationContext applicationContext) {
		super(applicationContext);
		this.repository = repository;
	}

	public boolean isDefaultStore() {
		return true;
	}
	
	public String getCacheName() {
		return "dbStoreCache";
	}
	
	@Override
	protected String lookupPropertyValue(AbstractPropertyTemplate template,
			SimpleResource resource) {
		return getProperty(resource, template.getResourceKey(), template.getDefaultValue());
	}
	
	public String getProperty(SimpleResource resource, String resourceKey, String defaultValue) {
		// Look up property on resource
		Property p = repository.getProperty(resourceKey, resource);
		if (p == null || p.getValue()==null) {
			return defaultValue;		
		}
		return p.getValue();
	}

	@Override
	public boolean hasPropertyValueSet(AbstractPropertyTemplate template,
			SimpleResource resource) {
		Property p = repository.getProperty(template.getResourceKey(), resource);
		return p!=null;
	}

	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			SimpleResource resource, String value) {
		setProperty(resource, template.getResourceKey(), value);
	}
	
	public void setProperty(SimpleResource resource, String resourceKey, String value) {
		DatabaseProperty property = repository.getProperty(
				resourceKey, resource);
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(resourceKey);
			if(resource!=null) {
				property.setResourceId(resource.getId());
			}
		}
		property.setValue(value);
		repository.saveProperty(property);
	}

	@Override
	protected String lookupPropertyValue(PropertyTemplate template) {
		DatabaseProperty property = repository.getProperty(template
				.getResourceKey());
		if (property == null) {
			return template.getDefaultValue();
		} else {
			return property.getValue();
		}
	}

	@Override
	protected void doSetProperty(PropertyTemplate template, String value) {
		
		DatabaseProperty property = repository.getProperty(template
				.getResourceKey());
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(template.getResourceKey());
		}
		property.setValue(value);
		repository.saveProperty(property);
		
	}

	@Override
	public void init(Element element) throws IOException {
		
	}

}
