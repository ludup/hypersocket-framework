package com.hypersocket.config;

import java.util.Map;

import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.PropertyTemplateRepository;

public interface SystemConfigurationRepository extends PropertyTemplateRepository {

	void setValues(Map<String, String> values);

	PropertyTemplate getPropertyTemplate(String resourceKey);

	
}
