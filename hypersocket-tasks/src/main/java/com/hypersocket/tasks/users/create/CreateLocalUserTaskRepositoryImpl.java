package com.hypersocket.tasks.users.create;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class CreateLocalUserTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements CreateLocalUserTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/createLocalUserTask.xml");
	}

}
