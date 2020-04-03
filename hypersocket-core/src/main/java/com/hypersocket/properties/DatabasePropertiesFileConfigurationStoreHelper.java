package com.hypersocket.properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.i18n.I18NService;

@Component
public class DatabasePropertiesFileConfigurationStoreHelper {

	public static final String RESOURCE_BUNDLE = "DatabaseService";

	@Autowired
	private I18NService i18nService;

	@PostConstruct
	public void init(){
		 i18nService.registerBundle(RESOURCE_BUNDLE);
	}

}
