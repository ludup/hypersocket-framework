package com.hypersocket.search.task;

import javax.annotation.PostConstruct;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

public class SearchResourceTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements SearchResourceTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/searchResourceTask.xml");
	}

}
