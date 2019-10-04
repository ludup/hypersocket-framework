package com.hypersocket.tasks.ip.resolve;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class ResolveIPTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements ResolveIPTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/resolveIPTask.xml");
	}

}
