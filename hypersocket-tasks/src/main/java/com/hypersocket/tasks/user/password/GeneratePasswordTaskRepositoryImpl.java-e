package com.hypersocket.tasks.user.password;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class GeneratePasswordTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements GeneratePasswordTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/generatePasswordTask.xml");
	}

}
