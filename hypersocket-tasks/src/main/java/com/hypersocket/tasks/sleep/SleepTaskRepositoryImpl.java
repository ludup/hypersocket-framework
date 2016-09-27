package com.hypersocket.tasks.sleep;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class SleepTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements SleepTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/sleepTask.xml");
	}

}
