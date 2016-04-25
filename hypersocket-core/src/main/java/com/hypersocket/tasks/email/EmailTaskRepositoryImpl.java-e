package com.hypersocket.tasks.email;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class EmailTaskRepositoryImpl extends ResourceTemplateRepositoryImpl
		implements EmailTaskRepository {

	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/sendEmail.xml");
	}

}
