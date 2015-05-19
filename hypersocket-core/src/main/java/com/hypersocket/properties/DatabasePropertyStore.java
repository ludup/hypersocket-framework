package com.hypersocket.properties;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.resource.AbstractResource;

@Transactional
public class DatabasePropertyStore extends AbstractResourcePropertyStore {

	PropertyRepository repository;
	ApplicationContext applicationContext;
	
	public DatabasePropertyStore(PropertyRepository repository, EncryptionService encryptionService) {
		super(encryptionService);
		this.repository = repository;
	}

	@Override
	protected String lookupPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		// Look up property on resource
		Property p = repository.getProperty(template.getResourceKey(), resource);
		if (p == null || p.getValue()==null) {
			// No value for resource, look for overridden default setting
			p = repository.getProperty(template.getResourceKey(), null);
			if(p==null) {
				// Return actual default
				return template.getDefaultValue();
			}		
		}
		
		return p.getValue();
	}

	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		DatabaseProperty property = repository.getProperty(
				template.getResourceKey(), resource);
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(template.getResourceKey());
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
