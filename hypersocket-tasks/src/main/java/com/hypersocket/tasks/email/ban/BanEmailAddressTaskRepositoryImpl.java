package com.hypersocket.tasks.email.ban;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class BanEmailAddressTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements BanEmailAddressTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/banEmailAddressTask.xml");
	}

}
