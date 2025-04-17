package com.hypersocket.tasks.reset.profile;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ResetProfileTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ResetProfileTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/resetProfileTask.xml");
	}

}
