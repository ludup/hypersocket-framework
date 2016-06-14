package com.hypersocket.inbox.tasks;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class CollectMailTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements CollectMailTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/collectMailTask.xml");
	}

}
