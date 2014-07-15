package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.hql.ast.tree.AbstractRestrictableStatement;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResource;

@Transactional
public class DatabasePropertyStore extends AbstractResourcePropertyStore {

	PropertyRepository repository;

	public DatabasePropertyStore(PropertyRepository repository) {
		this.repository = repository;
	}

	@Override
	protected String lookupPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		Property p = repository.getProperty(template.getResourceKey(), resource);
		if (p == null) {
			return template.getDefaultValue();
		} else {
			return p.getValue();
		}
	}

	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		DatabaseProperty property = repository.getProperty(
				template.getResourceKey(), resource);
		if (property == null) {
			property = new DatabaseProperty();
			property.setResourceKey(template.getResourceKey());
			property.setResourceId(resource.getId());
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

}
