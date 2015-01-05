package com.hypersocket.tasks.email;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
@Transactional
public class EmailTaskRepositoryImpl extends ResourceTemplateRepositoryImpl
		implements EmailTaskRepository {

	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/sendEmail.xml");
	}

}
