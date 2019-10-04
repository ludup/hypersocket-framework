package com.hypersocket.tasks.ip.resolve;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class LookupIPTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements LookupIPTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/lookupIPTask.xml");
	}

}
