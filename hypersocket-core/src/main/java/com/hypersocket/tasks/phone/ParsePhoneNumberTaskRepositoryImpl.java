package com.hypersocket.tasks.phone;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ParsePhoneNumberTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ParsePhoneNumberTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/parsePhoneNumberTask.xml");
	}

}
