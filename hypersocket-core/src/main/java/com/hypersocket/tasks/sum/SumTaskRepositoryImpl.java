package com.hypersocket.tasks.sum;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class SumTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements SumTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/sumTask.xml");
	}

}
