package com.hypersocket.tasks.count;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class CountTaskTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements CountTaskTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/countTaskTask.xml");
	}

}
