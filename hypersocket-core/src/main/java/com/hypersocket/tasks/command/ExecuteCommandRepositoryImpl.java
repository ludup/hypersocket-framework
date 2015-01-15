package com.hypersocket.tasks.command;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ExecuteCommandRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ExecuteCommandRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/executeCommand.xml");
	}

}
