package com.hypersocket.tasks.digest;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class DigestTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements DigestTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/digestTask.xml");
	}

}
