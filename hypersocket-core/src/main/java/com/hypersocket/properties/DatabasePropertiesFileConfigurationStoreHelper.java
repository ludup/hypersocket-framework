package com.hypersocket.properties;

import com.hypersocket.i18n.I18NService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabasePropertiesFileConfigurationStoreHelper {

	public static final String RESOURCE_BUNDLE = "DatabaseService";

	@Autowired
	I18NService i18nService;

	@PostConstruct
	public void init(){
		 i18nService.registerBundle(RESOURCE_BUNDLE);
	}

}
