package com.hypersocket.tasks.alert;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class AlertTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements AlertTaskRepository {

	public AlertTaskRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/generateAlert.xml");
	}

}
