package com.hypersocket.tasks.user;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class DisableAccountTaskRepositoryImpl extends
		ResourceTemplateRepositoryImpl implements DisableAccountTaskRepository {

	public DisableAccountTaskRepositoryImpl() {
		super(true);
	}
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("tasks/disableAccount.xml");
	}

}
