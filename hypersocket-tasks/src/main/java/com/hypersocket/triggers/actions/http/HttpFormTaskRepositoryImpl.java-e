package com.hypersocket.triggers.actions.http;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class HttpFormTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements HttpFormTaskRepository {

	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/httpForm.xml");
	}
}

