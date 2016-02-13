package com.hypersocket.tasks.sleep;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class SleepTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements SleepTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/sleepTask.xml");
	}

}
