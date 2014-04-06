package com.hypersocket.config;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.DatabasePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyRepositoryImpl;
import com.hypersocket.properties.PropertyTemplateRepository;
import com.hypersocket.properties.PropertyTemplateRepositoryAbstractImpl;

@Repository
@Transactional
public class ConfigurationRepositoryImpl extends PropertyRepositoryImpl implements ConfigurationRepository {

	PropertyTemplateRepository repository;
	
	@PostConstruct
	private void postConstruct() {
		repository = new PropertyTemplateRepositoryAbstractImpl(new DatabasePropertyStore(this));
	}
	
	@Override
	public void loadPropertyTemplates(String string) {
		repository.loadPropertyTemplates(string);
	}

	@Override
	public String getValue(String resourceKey) {
		return repository.getValue(resourceKey);
	}

	@Override
	public Integer getIntValue(String name) throws NumberFormatException {
		return repository.getIntValue(name);
	}

	@Override
	public Boolean getBooleanValue(String name) {
		return repository.getBooleanValue(name);
	}

	@Override
	public void setValue(String resourceKey, String value) {
		repository.setValue(resourceKey, value);
	}

	@Override
	public void setValue(String resourceKey, Integer value) {
		repository.setValue(resourceKey, value);
	}

	@Override
	public void setValue(String name, Boolean value) {
		repository.setValue(name, value);
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories() {
		return repository.getPropertyCategories();
	}

	@Override
	public String[] getValues(String name) {
		return repository.getValues(name);
	}

	@Override
	public void setValues(Map<String, String> values) {
		
		repository.setValues(values);
		
	}

	
}
