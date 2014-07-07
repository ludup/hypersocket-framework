package com.hypersocket.attributes;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.i18n.I18NService;

@Service
public class AttributeServiceImpl implements AttributeService {

	public static final String RESOURCE_BUNDLE = "UserAttributes";
	
	@Autowired
	I18NService i18nService;
	
	@PostConstruct
	private void postConstruct() {
		
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}


}
