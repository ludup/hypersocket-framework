package com.hypersocket.tasks.suspend;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class SuspendUserTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements SuspendUserTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/suspendUser.xml");
	}
}

