package com.hypersocket.tasks.ip.resolve;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class ResolveIPTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ResolveIPTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/resolveIPTask.xml");
	}

}
