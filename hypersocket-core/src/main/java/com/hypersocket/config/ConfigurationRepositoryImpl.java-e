package com.hypersocket.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.resource.AbstractResource;

@Repository
public class ConfigurationRepositoryImpl extends ResourceTemplateRepositoryImpl implements ConfigurationRepository {

	@PostConstruct
	private void postConstruct() {

	}

	@Override
	@Transactional
	public void setValues(AbstractResource resource, Map<String, String> values) {
		
		for(Map.Entry<String,String> e : values.entrySet()) {
			setValue(resource, e.getKey(), e.getValue());
		}
	}
}
