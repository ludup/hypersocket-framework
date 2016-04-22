package com.hypersocket.tasks.suspend;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class ResumeUserTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ResumeUserTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/resumeUser.xml");
	}
}

