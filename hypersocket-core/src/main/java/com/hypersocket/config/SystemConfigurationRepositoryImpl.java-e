package com.hypersocket.config;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.properties.DatabasePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyRepository;
import com.hypersocket.properties.PropertyRepositoryImpl;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.PropertyTemplateRepository;
import com.hypersocket.properties.PropertyTemplateRepositoryAbstractImpl;

@Repository
public class SystemConfigurationRepositoryImpl extends PropertyRepositoryImpl implements SystemConfigurationRepository {

	PropertyTemplateRepository repository;

	@Autowired
	EncryptionService encryptionService;

	@Autowired
	ApplicationContext applicationContext;

	@PostConstruct
	private void postConstruct() {
		repository = new PropertyTemplateRepositoryAbstractImpl(new DatabasePropertyStore(
				(PropertyRepository) applicationContext.getBean("systemConfigurationRepositoryImpl"),
				encryptionService)) {
			@SuppressWarnings("unchecked")
			protected <T> T getBean(String name, Class<T> clz) {
				return (T) applicationContext.getBean(name);
			}
		};
	}

	@Override
	public void loadPropertyTemplates(String string) {
		repository.loadPropertyTemplates(string);
	}

	@Override
	@Transactional(readOnly = true)
	public String getValue(String resourceKey) {
		return repository.getValue(resourceKey);
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getIntValue(String name) throws NumberFormatException {
		return repository.getIntValue(name);
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean getBooleanValue(String name) {
		return repository.getBooleanValue(name);
	}

	@Override
	@Transactional
	public void setValue(String resourceKey, String value) {
		repository.setValue(resourceKey, value);
	}

	@Override
	@Transactional
	public void setValue(String resourceKey, Integer value) {
		repository.setValue(resourceKey, value);
	}

	@Override
	@Transactional
	public void setValue(String name, Boolean value) {
		repository.setValue(name, value);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<PropertyCategory> getPropertyCategories() {
		return repository.getPropertyCategories();
	}

	@Override
	@Transactional(readOnly = true)
	public String[] getValues(String name) {
		return repository.getValues(name);
	}

	@Override
	@Transactional
	public void setValues(Map<String, String> values) {
		repository.setValues(values);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<PropertyCategory> getPropertyCategories(String group) {
		return repository.getPropertyCategories(group);
	}

	@Override
	@Transactional(readOnly = true)
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return repository.getPropertyTemplate(resourceKey);
	}

}
